package com.black.core.aop.ibatis;


import com.black.core.aop.code.HijackObject;
import com.black.core.mybatis.intercept.annotation.ClearSqlSessionLocalCache;
import com.black.core.mybatis.source.DynamicallyTransactionManager;
import com.black.core.mybatis.source.SessionLocalCacheManager;
import com.black.core.mybatis.source.TransactionIbtaisSessionHandler;
import com.black.core.mybatis.source.annotation.DynamicallySimpleRollBackTransactional;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@Log4j2
public class IbatisRollBackHandler {


    private final ThreadLocal<Method> tranStratMethod = new ThreadLocal<>();

    private final ThreadLocal<HashSet<String>> rollBackAliases = new ThreadLocal<>();

    public IbatisRollBackHandler() {
    }
    
    
    protected Object[] beforeInvoke(HijackObject hijack, Method method, Object[] args) {

        Method startMethod = tranStratMethod.get();
        if (startMethod == null){
            tranStratMethod.set(method);
//            Map<String, TransactionIbtaisSessionHandler> handlers = DynamicallyTransactionManager.getHandlers();
//            for (TransactionIbtaisSessionHandler handler : handlers.values()) {
//                handler.openTransaction();
//            }
            ClearSqlSessionLocalCache clearSqlSessionLocalCache;
            if ((clearSqlSessionLocalCache = AnnotationUtils.getAnnotation(method, ClearSqlSessionLocalCache.class)) != null){
                SessionLocalCacheManager.register(Arrays.asList(clearSqlSessionLocalCache.value()), true);
            }
        }
        return args;
    }
    
    protected void commit(HijackObject hijack, Method method) {
        Method tranMethod = tranStratMethod.get();
        if (tranMethod != null && tranMethod.equals(method)){
            try {
                //如果是最外层, 则清楚控制标记
                tranStratMethod.remove();
                Map<String, TransactionIbtaisSessionHandler> handlers = DynamicallyTransactionManager.getHandlers();
                for (String alias : handlers.keySet()) {
                    TransactionIbtaisSessionHandler handler = handlers.get(alias);
                    try {
                        //提交sql
                        handler.commit(true);
                    }catch (Throwable te){
                        CentralizedExceptionHandling.handlerException(te);
                        if (log.isInfoEnabled()) {
                            log.info("{} 数据源提交异常, 关闭此数据源", alias);
                        }
                    }
                }
            }finally {
                SessionLocalCacheManager.remove();
            }
        }
    }
    
    protected Object handlerException(Throwable e, HijackObject hijack, Method method) {
        Method tranMethod = tranStratMethod.get();
        String[] currenyAliases = getCurrenyMethodRollBackAlies(method);
        boolean cancel = false;
        Collection<String> clearAliases = new HashSet<>();
        try{
            //检查数据源
            Map<String, SqlSession> sqlSessions = DynamicallyTransactionManager.getSqlSessions();
            if (sqlSessions != null){
                for (String alias : sqlSessions.keySet()) {
                    clearAliases.add(alias);
                    SqlSession sqlSession = sqlSessions.get(alias);
                    Connection connection = sqlSession.getConnection();
                    if (connection == null || connection.isClosed()){
                        if (log.isInfoEnabled()) {
                            log.info("sqlsession 会话已经无效, {} 数据源即将关闭", alias);
                        }
                        clearSqlSession(alias);
                        cancel = true;
                    }
                }
            }
        }catch (Throwable te){
            CentralizedExceptionHandling.handlerException(te);
            if (log.isInfoEnabled()) {
                log.info("检验数据源有没有效时发生异常," +
                        " 现在关闭所有sqlsession, alias:{}", clearAliases);
            }
            for (String alias : clearAliases) {
                clearSqlSession(alias);
            }
            return null;
        }

        if (!cancel){
            if (tranMethod != null && tranMethod.equals(method)){
                try {
                    //由当前方法进行回滚
                    HashSet<String> sonMethodsAliases = rollBackAliases.get();
                    if (sonMethodsAliases == null){
                        sonMethodsAliases = new HashSet<>();
                        rollBackAliases.set(sonMethodsAliases);
                    }
                    HashSet<String> aliases = new HashSet<>(sonMethodsAliases);
                    if(currenyAliases != null){
                        aliases.addAll(Arrays.asList(currenyAliases));
                    }
                    Map<String, TransactionIbtaisSessionHandler> handlers = DynamicallyTransactionManager.getHandlers();
                    handlers.forEach((alias, handler) ->{
                        try {
                            if (aliases.contains(alias)){
                                handler.rollback();
                            }else {
                                handler.commit(true);
                            }
                        }catch (Throwable ex){
                            CentralizedExceptionHandling.handlerException(e);
                            //清除当前sqlSession
                            //下次会重新获取sqlsession
                            clearSqlSession(alias);
                        }
                    });

                }finally {
                    rollBackAliases.get().clear();
                    tranStratMethod.remove();
                    SessionLocalCacheManager.remove();
                }
            }else {
                if (currenyAliases != null){
                    rollBackAliases.get().addAll(Arrays.asList(currenyAliases));
                }
            }
        }

        return null;
    }


    private void clearSqlSession(String alias){
        DynamicallyTransactionManager.removeSqlSession(alias);
        DynamicallyTransactionManager.clear();
    }

    private String[] getCurrenyMethodRollBackAlies(Method method){
        DynamicallySimpleRollBackTransactional rollBack = AnnotationUtils.getAnnotation(method, DynamicallySimpleRollBackTransactional.class);
        if (rollBack != null){
            return rollBack.value();
        }
        return null;
    }
    
    
}
