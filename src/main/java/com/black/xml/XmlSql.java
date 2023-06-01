package com.black.xml;

import com.black.sql.QueryResultSetParser;
import com.black.sql_v2.Sql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-05-08 10:37
 */
@SuppressWarnings("all")
public class XmlSql {

    public static String PREFIX = "arg";

    private final static Map<String, XmlExecutor> EXECUTOR_CACHE = new ConcurrentHashMap<>();

    public static void setPREFIX(String PREFIX) {
        XmlSql.PREFIX = PREFIX;
    }

    public static String getDefaultName(){
        return Sql.DEFAULT_ALIAS;
    }

    public static Map<String, Object> castParams(Object... params){
        int index = 1;
        Map<String, Object> env = new LinkedHashMap<>();
        for (Object param : params) {
            env.put(PREFIX + index++, param);
        }
        return env;
    }


    public static QueryResultSetParser select(String id, Object... params){
        return opt().select(id, params);
    }

    public static void update(String id, Object... params){
        opt().update(id, params);
    }

    public static XmlExecutor opt(){
        return opt(getDefaultName());
    }

    public static XmlExecutor opt(String name){
        return EXECUTOR_CACHE.computeIfAbsent(name, XmlExecutor::new);
    }
}
