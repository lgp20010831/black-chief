package com.black.core.sql.code.util;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;
import com.black.core.sql.unc.SqlStatement;
import com.black.core.sql.unc.SqlWriter;
import lombok.NonNull;

import java.sql.Connection;
import java.util.Map;

public class Updator {

    static Log log = new SystemLog();

    static AliasColumnConvertHandler defaultHandler = new HumpColumnConvertHandler();


    public static void update(String proof,
                                                   String name,
                                                   @NonNull Map<String, Object> setMap,
                                                   String conditionSql){
        update(proof, name, setMap, null, conditionSql);
    }

    public static void update(String proof,
                                                   String name,
                                                   @NonNull Map<String, Object> setMap){
        update(proof, name, setMap, (Map<String, Object>) null);
    }

    public static void update(String proof,
                                                   String name,
                                                   @NonNull Map<String, Object> setMap,
                                                   Map<String, Object> argMap){
        update(proof, name, setMap, argMap, null);
    }

    public static void update(String proof,
                                                   String name,
                                                   @NonNull Map<String, Object> setMap,
                                                   Map<String, Object> argMap,
                                                   String applySql){
        update(proof, name, setMap, argMap, applySql, defaultHandler);
    }

    public static void update(String proof,
                                                   String name,
                                                   @NonNull Map<String, Object> setMap,
                                                   Map<String, Object> argMap,
                                                   String applySql,
                                                   AliasColumnConvertHandler handler){
        SqlStatement statement = SqlWriter.update(name);
        for (String key : setMap.keySet()) {
            String column = handler.convertColumn(key);
            statement.writeSet(column, SQLUtils.getString(argMap.get(key)), false);
        }
        if (argMap != null){
            for (String key : argMap.keySet()) {
                String column = handler.convertColumn(key);
                statement.writeEq(column, SQLUtils.getString(argMap.get(key)));
            }
        }
        statement.writeAft(applySql);
        update(proof, statement);
    }


    public static void update(String proof, SqlStatement sqlStatement){
        Connection connection = ConnectionManagement.getConnection(proof);
        String sql = sqlStatement.toString();
        if (log.isDebugEnabled()) {
            log.debug("==> updator run update: [" + sql + "]");
        }
        try {
            TransactionSQLManagement.transactionCall(() ->{
                SQLUtils.runSql(sql, connection);
                return null;
            });
        } catch (Throwable e) {
            throw new SQLSException(e);
        }
    }
}
