package com.black.core.sql.run;


import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.ImportPlatform;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.ConfigurationTreatment;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.result.ResultHandlerCollector;
import com.black.core.sql.code.session.PrepareStatementFactory;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.util.StringUtils;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class SqlRunner {

    private final Map<Method, ExecuteResultResolver> resultResolverMap = new ConcurrentHashMap<>();

    private final Map<Method, Configuration> configurationMap = new ConcurrentHashMap<>();

    public Object runSql(String sql,
                         boolean query,
                         Connection connection,
                         Log log,
                         MethodWrapper mw,
                         AliasColumnConvertHandler handler,
                         GlobalSQLConfiguration configuration) throws SQLException {
        if (log != null && log.isDebugEnabled()) {
            log.debug("==> inadvance sql: [" + sql + "]");
        }
        Method method = mw.getMethod();
        Object result = null;
        StatementWrapper statementWrapper = PrepareStatementFactory.getStatement(sql, configuration, connection, query);
        ExecuteBody executeBody =
        ApplicationUtil.programRunMills(() -> {
            return doExecute(statementWrapper, query);
        }, "sql runner", log, "===>> ");

        try {
            String finalSql = sql;
            Configuration config = configurationMap.computeIfAbsent(method, m -> {
                return createConfiguration(configuration, handler, finalSql, mw);
            });
            SQLMethodType sqlType = config.getMethodType();
            if (resultResolverMap.containsKey(method)){
                result = resultResolverMap.get(method).doResolver(executeBody,
                        config, mw, false);
            }else {
                for (ExecuteResultResolver resultResolver : ResultHandlerCollector.getResultResolvers()) {
                    if (resultResolver.support(sqlType, mw)){
                        result = resultResolver.doResolver(executeBody, config, mw, false);
                        resultResolverMap.put(method, resultResolver);
                        break;
                    }
                }
            }
            return result;
        }catch (SQLException e){
            throw new SQLSException("parse result error", e);
        }finally {
            statementWrapper.close();
        }
    }

    public ExecuteBody doExecute(StatementWrapper sw, boolean query) throws SQLException {
        ExecuteBody executeBody = new ExecuteBody(sw);
        if (query){
            ResultSet executeQuery = sw.executeQuery();
            executeBody.setQueryResult(executeQuery);
        }else {
            int update = sw.executeUpdate();
            executeBody.setUpdateCount(update);
        }
        return executeBody;
    }

    Configuration createConfiguration(GlobalSQLConfiguration globalSQLConfiguration, AliasColumnConvertHandler handler, String sql, MethodWrapper mw){
        Configuration cf = new Configuration(globalSQLConfiguration, mw);
        cf.setColumnConvertHandler(handler);
        cf.setMethodType(getSqlType(sql));
        cf.setCw(mw.getDeclaringClassWrapper());
        ClassWrapper<?> wrapper = cf.getCw();
        if(wrapper.inlayAnnotation(ImportPlatform.class)){
            wrapper = ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value());
        }
        return ConfigurationTreatment.treatmentConfig(cf, wrapper);
    }

    public static SQLMethodType getSqlType(@NonNull String sql){
        sql = StringUtils.removeLines(sql);
        if (sql.startsWith("select") || sql.startsWith("SELECT") )
            return SQLMethodType.QUERY;
        else if (sql.startsWith("insert") || sql.startsWith("INSERT"))
            return SQLMethodType.INSERT;
        else if (sql.startsWith("delete") || sql.startsWith("DELETE"))
            return SQLMethodType.DELETE;
        else if (sql.startsWith("update") || sql.startsWith("UPDATE"))
            return SQLMethodType.UPDATE;
        throw new SQLSException("not resolver sql type");
    }

}
