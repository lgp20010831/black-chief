package com.black.core.sql;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.Av0;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HumpColumnConvertHandler implements AliasColumnConvertHandler {

    //c -> a
    private final static Map<String, String> aliasCache = new ConcurrentHashMap<>();

    //a -> c
    private final static Map<String, String> columnCache = new ConcurrentHashMap<>();

    @Override
    public String convertColumn(String alias) {
        return columnCache.computeIfAbsent(alias, Av0::unruacnl);
    }

    @Override
    public String convertAlias(String columnName) {
        return aliasCache.computeIfAbsent(columnName, Av0::ruacnl);
    }

    public static void clear(){
        getColumnCache().clear();
        getAliasCache().clear();
    }

    public static Map<String, String> getAliasCache() {
        return aliasCache;
    }

    public static Map<String, String> getColumnCache() {
        return columnCache;
    }
}
