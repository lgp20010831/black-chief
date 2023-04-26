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


import java.sql.Connection;
import java.util.Map;

public class Deletor {


    static Log log = new SystemLog();

    static AliasColumnConvertHandler defaultHandler = new HumpColumnConvertHandler();

    public static void delete(String proof,
                                                   String name,
                                                   String conditionSql){
        delete(proof, name, null, conditionSql);
    }

    public static void delete(String proof,
                                                   String name){
        delete(proof, name, (Map<String, Object>) null);
    }

    public static void delete(String proof,
                                                   String name,
                                                   Map<String, Object> argMap){
        delete(proof, name, argMap, null);
    }

    public static void delete(String proof,
                                                   String name,
                                                   Map<String, Object> argMap,
                                                   String applySql){
        delete(proof, name, argMap, applySql, defaultHandler);
    }

    public static void delete(String proof,
                                                   String name,
                                                   Map<String, Object> argMap,
                                                   String applySql,
                                                   AliasColumnConvertHandler handler){
        SqlStatement statement = SqlWriter.delete(name);
        if (argMap != null){
            for (String key : argMap.keySet()) {
                String column = handler.convertColumn(key);
                statement.writeEq(column, SQLUtils.getString(argMap.get(key)), false);
            }
        }
        statement.writeAft(applySql);
        delete(proof, statement);
    }


    public static void delete(String proof, SqlStatement sqlStatement){
        Connection connection = ConnectionManagement.getConnection(proof);
        String sql = sqlStatement.toString();
        if (log.isDebugEnabled()) {
            log.debug("==> deletor run delete: [" + sql + "]");
        }
        try {
            TransactionSQLManagement.transactionCall(() ->{
                SQLUtils.runSql(sql, connection);
                return null;
            }, proof);
        } catch (Throwable e) {
            throw new SQLSException(e);
        }
    }

}
