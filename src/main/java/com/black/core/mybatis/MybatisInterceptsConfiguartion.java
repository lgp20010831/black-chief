package com.black.core.mybatis;

import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class MybatisInterceptsConfiguartion {

    public static final String DEFAULT_QUERY = "query";
    public static final String DEFAULT_PREPARE = "prepare";
    public static final String DEFAULT_RESULT = "result";
    public static final String DEFAULT_SET_PARAM = "param";

    public MybatisInterceptsConfiguartion(){
        //加载固定拦截的四个方法
        initIntercepts();
    }

    private final Map<Method, String> interceptsMethodsAliasMap = new HashMap<>(4);

    protected void initIntercepts(){
        try {
            interceptsMethodsAliasMap.put(
                    Executor.class.getMethod("query",
                            MappedStatement.class,
                            Object.class,
                            RowBounds.class,
                            ResultHandler.class), DEFAULT_QUERY);

            interceptsMethodsAliasMap.put(
                    StatementHandler.class.getMethod("prepare",
                            Connection.class, Integer.class), DEFAULT_PREPARE);

            interceptsMethodsAliasMap.put(
                    ResultSetHandler.class.getMethod("handleResultSets", Statement.class),
                    DEFAULT_RESULT);

            interceptsMethodsAliasMap.put(
                    ParameterHandler.class.getMethod("setParameters",
                            PreparedStatement.class), DEFAULT_SET_PARAM);
        } catch (NoSuchMethodException e) {
            CentralizedExceptionHandling.handlerException(e);
            throw new RuntimeException("加载拦截方法失败");
        }
    }

    public boolean validAlias(String alias){
        return interceptsMethodsAliasMap.containsValue(alias);
    }

    public String queryAlias(Method method){
        return interceptsMethodsAliasMap.get(method);
    }

    public String[] getAliases(){
        return interceptsMethodsAliasMap.values().toArray(new String[0]);
    }
}
