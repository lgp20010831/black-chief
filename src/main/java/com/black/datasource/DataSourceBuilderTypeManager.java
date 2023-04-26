package com.black.datasource;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.sql.code.DataSourceBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class DataSourceBuilderTypeManager {


    private static final Map<Class<? extends DataSourceBuilder>, DataSourceBuilder> cache = new ConcurrentHashMap<>();


    public static <T extends DataSourceBuilder> T getBuilder(Class<T> type){
        return (T) cache.computeIfAbsent(type, t -> {
            return InstanceBeanManager.instance(t, InstanceType.REFLEX_AND_BEAN_FACTORY);
        });
    }
}
