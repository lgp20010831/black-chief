package com.black.core.sql.code.util;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.BaseSQLApplicationContext;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;
import com.black.core.sql.unc.SqlStatement;
import com.black.core.sql.unc.SqlWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Selector {

    static Log log = new SystemLog();

    static AliasColumnConvertHandler defaultHandler = new HumpColumnConvertHandler();

    public static List<Map<String, Object>> select(String proof,
                                                   String name,
                                                   String conditionSql){
        return select(proof, name, null, conditionSql);
    }

    public static List<Map<String, Object>> select(String proof,
                                                   String name){
        return select(proof, name, (Map<String, Object>) null);
    }

    public static List<Map<String, Object>> select(String proof,
                                                   String name,
                                                   Map<String, Object> argMap){
        return select(proof, name, argMap, null);
    }

    public static List<Map<String, Object>> select(String proof,
                                                   String name,
                                                   Map<String, Object> argMap,
                                                   String applySql){
        return select(proof, name, argMap, applySql, defaultHandler);
    }

    public static List<Map<String, Object>> select(String proof,
                                                   String name,
                                                   Map<String, Object> argMap,
                                                   String applySql,
                                                   AliasColumnConvertHandler handler){
        SqlStatement statement = SqlWriter.select(name);
        if (argMap != null){
            for (String key : argMap.keySet()) {
                String column = handler.convertColumn(key);
                statement.writeEq(column, SQLUtils.getString(argMap.get(key)), false);
            }
        }
        statement.writeAft(applySql);
        return select(proof, statement, handler);
    }

    public static List<Map<String, Object>> select(String proof, SqlStatement sqlStatement){
        return select(proof, sqlStatement, defaultHandler);
    }

    public static List<Map<String, Object>> select(String proof, SqlStatement sqlStatement, AliasColumnConvertHandler handler){
        Connection connection = ConnectionManagement.getConnection(proof);
        String sql = sqlStatement.toString();
        if (log.isDebugEnabled()) {
            log.debug("==> selector run select: [" + sql + "]");
        }
        try {
            return SQLUtils.runJavaSelect(sql, connection, handler);
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public static SourceSelector source(){
        return source(BaseSQLApplicationContext.DEFAULT_ALIAS);
    }

    public static SourceSelector source(String alias){
        return new SourceSelector(alias);
    }

}
