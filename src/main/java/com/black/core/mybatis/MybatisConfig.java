package com.black.core.mybatis;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;

@Getter
@Setter
public class MybatisConfig {

    public static final String GET_BOUND_SQL = "getBoundSql";

    public static final String QUERY = "query";

    public static final String PREPARE = "prepare";

    public static final String HANDLER_RESULT_SETS = "handleResultSets";

    public static final String SET_PARAMETERS = "setParameters";

    boolean mybatisLogoInfo = false;

    /***
     * mybatis 提供的可以拦截的所有接口类
     */
    public static Class<?>[] interceptsMybatisInterfaces = new Class<?>[]{
            Executor.class, StatementHandler.class, ParameterHandler.class, ResultSetHandler.class
    };
}
