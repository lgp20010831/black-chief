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

    public static QueryResultSetParser selectByArray(String id, Object... params){
        return select(id, castParams(params));
    }

    public static QueryResultSetParser select(String id, Map<String, Object> env){
        return opt().select(id, env);
    }

    public static void updateByArray(String id, Object... params){
        update(id, castParams(params));
    }

    public static void update(String id, Map<String, Object> env){
        opt().update(id, env);
    }

    public static XmlExecutor opt(){
        return opt(getDefaultName());
    }

    public static XmlExecutor opt(String name){
        return EXECUTOR_CACHE.computeIfAbsent(name, XmlExecutor::new);
    }
}
