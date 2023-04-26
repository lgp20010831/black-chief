package com.black.core.sql.code;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.sql.code.mapping.Mapper;
import com.black.core.sql.code.mapping.MapperImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapperContextProxyManager {

    private Map<Class<? extends Mapper>, MapperImpl> implCache = new ConcurrentHashMap<>();

    public synchronized MapperImpl getAndCreate(Class<? extends Mapper> type,
                                                GlobalSQLConfiguration configuration,
                                                GlobalParentMapping parentMapping){
        return implCache.computeIfAbsent(type, t -> {
            return new MapperImpl(configuration, parentMapping, t);
        });
    }
}
