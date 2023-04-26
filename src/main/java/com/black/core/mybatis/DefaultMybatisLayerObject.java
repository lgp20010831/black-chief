package com.black.core.mybatis;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Invocation;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class DefaultMybatisLayerObject implements MybatisLayerObject{

    private final MybatisLayerQueue mybatisLayerQueue;
    private volatile LayerWrapper pointLayerWrapper;
    private volatile String pointAlias;
    private final ThreadLocal<AtomicInteger> index = new ThreadLocal<>();
    private volatile Invocation invocation;
    private final MybatisInterceptsConfiguartion configuartion;

    public DefaultMybatisLayerObject(MybatisLayerQueue mybatisLayerQueue,
                                     MybatisInterceptsConfiguartion configuartion) {
        this.mybatisLayerQueue = mybatisLayerQueue;
        this.configuartion = configuartion;
    }


    @Override
    public Collection<String> getAliases() {
        return pointLayerWrapper.aliases();
    }

    @Override
    public String pointAlias() {
        return pointAlias;
    }

    @Override
    public Object doChain() {
        AtomicInteger threadIndex = index.get();
        if (threadIndex == null){
            index.set(threadIndex = new AtomicInteger(0));
        }
        threadIndex.incrementAndGet();
        final int i = threadIndex.get();
        if (i >= mybatisLayerQueue.size()){
            if (log.isWarnEnabled()) {
                log.warn("异常下标: {}", i);
            }
            return mybatisLayerQueue.get(mybatisLayerQueue.size() - 1).getTarget().doIntercept(this);
        }
        pointLayerWrapper = mybatisLayerQueue.get(threadIndex.get());
        return pointLayerWrapper.getTarget().doIntercept(this);
    }

    @Override
    public boolean isExecutor() {
        return invocation.getTarget() instanceof Executor;
    }

    @Override
    public boolean isStatementHandler() {
        return invocation.getTarget() instanceof StatementHandler;
    }

    @Override
    public boolean isResultSetHandler() {
        return invocation.getTarget() instanceof ResultSetHandler;
    }

    @Override
    public boolean isParameterHandler() {
        return invocation.getTarget() instanceof ParameterHandler;
    }

    @Override
    public Executor getExecutor() {
        return (Executor) invocation.getTarget();
    }

    @Override
    public StatementHandler getStatementHandler() {
        return (StatementHandler) invocation.getTarget();
    }

    @Override
    public ResultSetHandler getResultSetHandler() {
        return (ResultSetHandler) invocation.getTarget();
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return (ParameterHandler) invocation.getTarget();
    }

    @Override
    public boolean isLastLayer() {
        if (mybatisLayerQueue.size() == 1){
            return true;
        }
        AtomicInteger integer = index.get();
        if (integer == null){
            index.set(integer = new AtomicInteger(0));
        }
        return integer.get() == mybatisLayerQueue.size() - 2;
    }

    @Override
    public Object[] interceptsArgs() {
        return invocation.getArgs();
    }

    @Override
    public Invocation getInvocation() {
        return invocation;
    }

    @Override
    public void reset(Invocation invocation) {
        this.invocation = invocation;
        pointAlias = configuartion.queryAlias(invocation.getMethod());
        AtomicInteger currenyIndex = index.get();
        if (currenyIndex == null){
            index.set(currenyIndex = new AtomicInteger(0));
        }
        currenyIndex.set(0);
        pointLayerWrapper = mybatisLayerQueue.get(0);
    }
}
