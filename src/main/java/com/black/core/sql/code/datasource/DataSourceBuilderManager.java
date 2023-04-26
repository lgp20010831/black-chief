package com.black.core.sql.code.datasource;

import com.black.function.Supplier;
import com.black.core.sql.code.DataSourceBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceBuilderManager {


    private static Map<String, DataSourceBuilder> datasourceBuilderCache = new ConcurrentHashMap<>();

    public static DataSourceBuilder obtain(String alias, Supplier<DataSourceBuilder> supplier){
        if (datasourceBuilderCache.containsKey(alias)) {
            return datasourceBuilderCache.get(alias);
        }

        try {
            DataSourceBuilder sourceBuilder = supplier.get();
            datasourceBuilderCache.put(alias, sourceBuilder);
            return sourceBuilder;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static void clear(){
        datasourceBuilderCache.clear();
    }
}
