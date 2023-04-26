package com.black.core.sql.code;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.sql.entity.EntityImpl;
import com.black.core.sql.entity.EntityMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityContextProxyManager {

    private Map<Class<? extends EntityMapper<?>>, EntityImpl> implCache = new ConcurrentHashMap<>();

    public synchronized EntityImpl getAndCreate(Class<? extends EntityMapper<?>> type,
                                                GlobalSQLConfiguration configuration,
                                                GlobalParentMapping parentMapping){
        return implCache.computeIfAbsent(type, t -> {
            return new EntityImpl(configuration, parentMapping, t);
        });
    }
}
