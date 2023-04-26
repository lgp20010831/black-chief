package com.black.core.sql.run;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.*;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.util.StringUtils;


import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RunSqlProcessor extends SqlRunner{

    private final Map<Method, ExecuteResultResolver> resultResolverMap = new ConcurrentHashMap<>();

    public Object invoke(GlobalSQLConfiguration configuration,
                         RunConfiguration runConfiguration,
                         Object[] args,
                         MethodWrapper mw) throws Throwable {
        Method method = mw.getMethod();
        Log log = configuration.getLog();
        Connection connection = ConnectionManagement.getConnection(configuration.getDataSourceAlias());
        return TransactionSQLManagement.transactionCall(() ->{
            Object result = null;
            for (String sql : runConfiguration.getSqls()) {
                sql = StringUtils.removeFrontSpace(sql);
                try {

                    sql = GlobalMapping.parseAndObtain(sql, true);
                    sql = RunSqlParser.parseSql(sql, mw, args);
                    boolean query = StringUtils.startsWithIgnoreCase(sql, "select");
                    //sql.startsWith("select") || sql.startsWith("SELECT");
                    if (query){
                        for (GlobalSQLRunningListener listener : configuration.getApplicationContext().getSQLRunningListeners()) {
                            sql = listener.postRunScriptSelectSql(configuration, sql);
                        }
                    }else {
                        for (GlobalSQLRunningListener listener : configuration.getApplicationContext().getSQLRunningListeners()) {
                            sql = listener.postRunScriptExecuteSql(configuration, sql);
                        }
                    }
                    try {
                        result = runSql(sql, query, connection, log, mw, runConfiguration.getHandler(), configuration);
                    }catch (SQLException e){
                        throw new SQLSException("parse result error", e);
                    }
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                    if (runConfiguration.isStopOnError()){
                        throw new SQLSException(e);
                    }
                }finally {
                    ResultSetThreadManager.close();
                }
            }
            return result;
        }, configuration.getDataSourceAlias());
    }
}
