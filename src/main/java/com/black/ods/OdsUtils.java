package com.black.ods;

import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.core.sql.code.util.SQLUtils;
import com.black.sql.Query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OdsUtils {

    private static final IoLog log = new CommonLog4jLog();

    public static Map<Object, Object> castParamMap(Object... args){
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        for (int i = 1; i <= args.length; i++) {
            map.put(i, args[i - 1]);
        }
        return map;
    }

    public static Object[] getInjectArgs(Map<String, Object> map, String[] paramInjectArray){
        Object[] args;
        if (paramInjectArray != null){
            args = new Object[paramInjectArray.length];
            for (int i = 0; i < paramInjectArray.length; i++) {
                String paramName = paramInjectArray[i];
                args[i] = map.get(paramName);
            }
        }else {
            args = map.values().toArray(new Object[0]);
        }
        return args;
    }

    public static void doExecuteUpdate(String sql, Connection connection, Object... args) throws SQLException {
        Map<Object, Object> paramMap = OdsUtils.castParamMap(args);
        sql = Query.doParseSql(sql, paramMap);
        log.info("ods execute update sql: [{}]", sql);
        connection.createStatement().executeUpdate(sql);
    }

    public static Object doExecuteQuery(String sql, Connection connection, Object... args) throws SQLException {
        Map<Object, Object> paramMap = OdsUtils.castParamMap(args);
        sql = Query.doParseSql(sql, paramMap);
        log.info("ods execute query sql: [{}]", sql);
        ResultSet resultSet = connection.createStatement().executeQuery(sql);
        List<LinkedHashMap> linkedHashMaps = SQLUtils.parseResultSet(resultSet, LinkedHashMap.class);
        return linkedHashMaps;
    }


}
