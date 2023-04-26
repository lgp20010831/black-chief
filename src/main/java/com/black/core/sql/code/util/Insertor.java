package com.black.core.sql.code.util;


import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Insertor {

    static Log log = new SystemLog();

    static AliasColumnConvertHandler defaultHandler = new HumpColumnConvertHandler();

    public static void add(String proof, String name, Map<String, Object> map){
        add(proof, name, Collections.singletonList(map), defaultHandler);
    }

    public static void add(String proof, String name, Map<String, Object> map, AliasColumnConvertHandler handler){
        add(proof, name, Collections.singletonList(map), handler);
    }

    public static void add(String proof, String name, List<Map<String, Object>> map){
        add(proof, name, map, defaultHandler);
    }

    public static void add(String proof, String name, List<Map<String, Object>> map, AliasColumnConvertHandler handler){
        Connection connection = ConnectionManagement.getConnection(proof);
        try {
            TransactionSQLManagement.transactionCall(() ->{
                SQLUtils.insertList(name, map, connection, handler);
                return null;
            }, proof);
        } catch (Throwable e) {
            throw new SQLSException(e);
        }
    }
}
