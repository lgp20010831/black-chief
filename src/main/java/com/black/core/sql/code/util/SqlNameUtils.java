package com.black.core.sql.code.util;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlNameUtils {


    static Map<String, String> cache = new ConcurrentHashMap<>();

    public static String parseNameOfTableName(Configuration configuration, String methodName){
        return cache.computeIfAbsent(methodName, mn ->{
            switch (configuration.getMethodType()){
                case UPDATE:
                    return configuration.convertColumn(StringUtils.removeIfStartWiths(methodName, "update", "modify", "alter", "revise", "amend", "upd"));
                case INSERT:
                    return configuration.convertColumn(StringUtils.removeIfStartWiths(methodName, "insert", "add", "save", "join", "push", "put", "increase"));
                case QUERY:
                    return configuration.convertColumn(StringUtils.removeIfStartWiths(methodName, "select", "get", "query", "find", "obtain", "read", "is", "list", "exist", "have", "was"));
                case DELETE:
                    return configuration.convertColumn(StringUtils.removeIfStartWiths(methodName, "delete", "del", "remove", "cut", "out", "clear", "strike"));
                default:
                    throw new IllegalStateException("sql method type error");
            }
        });
    }

    public static SQLMethodType parseName(MethodWrapper mw){
        String name = mw.getName();
        if (name.startsWith("select") ||
        name.startsWith("get") ||
        name.startsWith("query") ||
        name.startsWith("find") ||
        name.startsWith("obtain") ||
        name.startsWith("read") ||
        name.startsWith("is") ||
        name.startsWith("list") ||
        name.startsWith("exist") ||
        name.startsWith("have") ||
        name.startsWith("was"))
            return SQLMethodType.QUERY;
        else if (name.startsWith("insert")||
        name.startsWith("add") ||
        name.startsWith("save") ||
        name.startsWith("join") ||
        name.startsWith("push") ||
        name.startsWith("put") ||
        name.startsWith("increase"))
            return SQLMethodType.INSERT;
        else if (name.startsWith("update") ||
        name.startsWith("modify") ||
        name.startsWith("alter") ||
        name.startsWith("revise") ||
        name.startsWith("amend") ||
        name.startsWith("upd"))
            return SQLMethodType.UPDATE;
        else if (name.startsWith("delete") ||
        name.startsWith("del") ||
        name.startsWith("remove") ||
        name.startsWith("cut") ||
        name.startsWith("out") ||
        name.startsWith("clear")||
        name.startsWith("strike"))
            return SQLMethodType.DELETE;
        throw new IllegalStateException("can not parse method sql method(查看 SqlNameUtils 了解如何命名)");
    }


}
