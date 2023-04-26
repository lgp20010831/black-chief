package com.black.core.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Invocation;

import java.util.Collection;

public interface MybatisLayerObject {

    Collection<String> getAliases();

    String pointAlias();

    Object doChain();

    boolean isExecutor();
    
    boolean isStatementHandler();
    
    boolean isResultSetHandler();

    boolean isParameterHandler();

    Executor getExecutor();

    StatementHandler getStatementHandler();

    ResultSetHandler getResultSetHandler();

    ParameterHandler getParameterHandler();

    boolean isLastLayer();

    Object[] interceptsArgs();

    Invocation getInvocation();

    void reset(Invocation invocation);
}
