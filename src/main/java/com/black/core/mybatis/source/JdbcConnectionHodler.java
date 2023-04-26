package com.black.core.mybatis.source;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class JdbcConnectionHodler {

    private static final Map<String, Connection> connectionCache = new HashMap<>();

    static void saveConnection(String alias, Connection connection){
         connectionCache.put(alias, connection);
    }

    public static Connection getConnection(String alias){
        return connectionCache.get(alias);
    }
}
