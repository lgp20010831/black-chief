package com.black.core.mybatis.source;

import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.spring.util.ApplicationUtil;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class IbtaisMapperProxy implements AgentLayer {
    public static boolean logInvokeTime = true;
    private final String alias;
    private final IbatisDataSourceGroupConfigurer configurer;
    private final SqlSessionFactory sqlSessionFactory;
    private final ExecutorType executorType;
    private final PersistenceExceptionTranslator exceptionTranslator;
    private final Map<Method, MapperMethodWrapper> mapperProxyCache = new HashMap<>();
    private final Map<MapperMethodWrapper, SqlSession> sqlSessionCache = new HashMap<>();
    private final Map<SqlSession, TransactionIbtaisSessionHandler> handlerCache = new HashMap<>();

    public IbtaisMapperProxy(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                             String alias, IbatisDataSourceGroupConfigurer configurer) {
        this(sqlSessionFactory, executorType, null, alias, configurer);
    }

    public IbtaisMapperProxy(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                             PersistenceExceptionTranslator exceptionTranslator, String alias,
                             IbatisDataSourceGroupConfigurer configurer) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.configurer = configurer;
        this.alias = alias;
        if (exceptionTranslator == null){
            this.exceptionTranslator = new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
        }else {
            this.exceptionTranslator = exceptionTranslator;
        }
    }

    protected TransactionIbtaisSessionHandler obtainSessionHandler(SqlSession session){
        return new DefaultTransactionHandler(alias, session);
    }

    protected void registerHandler(TransactionIbtaisSessionHandler handler){
        DynamicallyTransactionManager.registerHandler(handler);
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Class<?> targetClazz = layer.getAgentClazz();
        Method proxyMethod = layer.getProxyMethod();
        IbatisComponentGiver ibatisComponentGiver = IbatisGiverManager.getIbatisComponentGiver();
        MapperMethodWrapper mapperMethod = mapperProxyCache.computeIfAbsent(proxyMethod, i -> {
            return ibatisComponentGiver.createMapperMethod(targetClazz, proxyMethod, sqlSessionFactory.getConfiguration());
        });

        SqlSession session= DynamicallySqlSessionUtil.getSqlSession(alias, sqlSessionFactory, executorType,
                exceptionTranslator, configurer);
        TransactionIbtaisSessionHandler handler = obtainSessionHandler(session);
        registerHandler(handler);
        Object unwrapped;
        try {
            if (SessionLocalCacheManager.needClearCache(alias)) {
                session.clearCache();
            }
            Object execute;
            if (logInvokeTime){
                SqlSession fs = session;
                execute = ApplicationUtil.programRunMills(() ->{
                    return mapperMethod.execute(fs, layer.getArgs());
                }, "数据源: " + alias + " 执行sql");
            }else {
                execute = mapperMethod.execute(session, layer.getArgs());
            }

            //如果当前不再事务存活范围内
            if (!DynamicallyTransactionManager.isActivityTransaction(alias)) {
                session.commit(true);
            }
            unwrapped = execute;
        }catch (Throwable e){
            unwrapped = ExceptionUtil.unwrapThrowable(e);
            if (exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                Throwable translated = exceptionTranslator.translateExceptionIfPossible((PersistenceException)unwrapped);
                if (translated != null) {
                    unwrapped = translated;
                }
            }
            throw ((Throwable)unwrapped);
        }finally {
            if (!DynamicallyTransactionManager.isActivityTransaction(alias)) {
                //如果不再事务监控中, 则关闭 session
                DynamicallySqlSessionUtil.closeSqlSession(alias, session);
            }
        }
        return unwrapped;
    }
}
