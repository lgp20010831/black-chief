package com.black.db;

import com.black.core.json.ReflexUtils;
import com.black.core.log.IoLog;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.SetGetUtils;


import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBS {

    private static DBGlobalConfiguration configuration;


    public static DBGlobalConfiguration getConfiguration() {
        if (configuration == null){
            configuration = new DBGlobalConfiguration();
            configuration.init();
        }
        return configuration;
    }

    public static <T> T query(DBConnection connection,
                              String sql,
                              Class<T> resultType,
                              Object... params){
        return SQLUtils.getSingle(queryList(connection, sql, resultType, params));
    }

    public static Map<String, Object> queryMap(DBConnection connection,
                                               String sql,
                                               Object... params){
        return SQLUtils.getSingle(queryMapList(connection, sql, params));
    }

    public static List<Map<String, Object>> queryMapList(DBConnection connection,
                                                         String sql,
                                                         Object... params){
        Object list = queryList(connection, sql, LinkedHashMap.class, params);
        return (List<Map<String, Object>>) list;
    }

    public static <T> List<T> queryList(DBConnection connection,
                                        String sql,
                                        Class<T> resultType,
                                        Object... params){
        DBGlobalConfiguration configuration = getConfiguration();
        AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        IoLog log = configuration.getLog();
        Connection fetchConnection = connection.getFetchConnection();
        try {
            PreparedStatement statement = fetchConnection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            if (log.isInfoEnabled()) {
                log.info("[DB] query sql: {}", sql);
            }
            ResultSet resultSet = statement.executeQuery();
            return parseJavaResult(resultSet, resultType, convertHandler);
        }catch (SQLException e){
            if (log.isErrorEnabled()) {
                log.error("[DB] running sql error: {}", e.getMessage());
            }
            throw new SQLSException(e);
        }finally {
            if (!connection.isTransactionActivity()) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new SQLSException(e);
                }
            }
        }
    }


    public static <T> List<T> parseJavaResult(ResultSet resultSet, Class<T> mapType, AliasColumnConvertHandler handler){
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()){
                T bean = ReflexUtils.instance(mapType);
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    String javaName = handler == null ? columnName : handler.convertAlias(columnName);
                    Object value = resultSet.getObject(i);
                    if (bean instanceof Map){
                        Map<String, Object> map = (Map<String, Object>) bean;
                        map.put(javaName, value);
                    }else {
                        SetGetUtils.invokeSetMethod(javaName, value, bean);
                    }

                }
                list.add(bean);
            }
            SQLUtils.closeResultSet(resultSet);
            return list;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

}
