package com.black.core.mybatis;



import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


@Intercepts({
        @Signature(type = Executor.class, method = MybatisConfig.QUERY,
                args = {MappedStatement.class,
                        Object.class,
                        RowBounds.class,
                        ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = MybatisConfig.PREPARE,
                args = {Connection.class, Integer.class}),
        @Signature(type = ResultSetHandler.class, method = MybatisConfig.HANDLER_RESULT_SETS,
                args = Statement.class),
        @Signature(type = ParameterHandler.class, method = MybatisConfig.SET_PARAMETERS,
                args = PreparedStatement.class)
})
@Log4j2
public class MybatisInterceptsDispatcher implements Interceptor, IbatisIntercept {

    /***
     *
     *   拦截器调度中心,拦截所有的接口信息，然后分发给子拦截器
     *   prepare: target = {@link org.apache.ibatis.executor.statement.RoutingStatementHandler
     *      ↓
     *   setParameters   target = {@link org.apache.ibatis.scripting.defaults.DefaultParameterHandler}
     *      ↓           参数都是{@link com.zaxxer.hikari.pool.HikariProxyPreparedStatement}
     *   handleResultSets target = {@link org.apache.ibatis.executor.resultset.DefaultResultSetHandler}
     *
     *   起始只拦截四个方法， 如果想增加拦截其他方法...(暂不支持)
     *
     *
     *
     */

    public static boolean printLogo = true;

    private MybatisInterceptsConfiguartion mybatisInterceptsConfiguartion;

    private final Map<String, MybatisLayerQueue> mybatisLayers = new HashMap<>();

    private final Map<Method, MybatisLayerObject> methodMybatisLayerObjectMap = new HashMap<>(8);

    public MybatisInterceptsDispatcher() {
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (printLogo){
            if (log.isDebugEnabled()) {
                log.debug("mybatis 拦截器执行: {}", method.getName());
            }
        }
        if (mybatisInterceptsConfiguartion != null){
            String alias = mybatisInterceptsConfiguartion.queryAlias(method);
            if (alias != null){
                MybatisLayerQueue layerQueue = mybatisLayers.get(alias);
                if (layerQueue != null){

                    MybatisLayerObject mybatisLayerObject =
                            methodMybatisLayerObjectMap.computeIfAbsent(method, k -> createLayerObject(layerQueue));
                    mybatisLayerObject.reset(invocation);
                    return layerQueue.invoke(mybatisLayerObject);
                }
            }
        }
        return invocation.proceed();
    }


    private MybatisLayerObject createLayerObject(MybatisLayerQueue queue){
        return new DefaultMybatisLayerObject(queue, mybatisInterceptsConfiguartion);
    }

    public void setMybatisInterceptsConfiguartion(MybatisInterceptsConfiguartion mybatisInterceptsConfiguartion) {
        this.mybatisInterceptsConfiguartion = mybatisInterceptsConfiguartion;
    }

    public void add(Map<String, MybatisLayerQueue> mybatisLayers){
        if (mybatisLayers != null){
            this.mybatisLayers.putAll(mybatisLayers);
        }
    }

    @Override
    public Object doIntercept(MybatisLayerObject layer) {
        try {
            return layer.getInvocation().proceed();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
