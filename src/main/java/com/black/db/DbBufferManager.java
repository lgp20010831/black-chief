package com.black.db;

import com.black.core.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DbBufferManager {

    private static Map<String, DbBuffer> bufferCache = new ConcurrentHashMap<>();

    public static DbBuffer spring(){
        return alloc("spring");
    }

    public static DbBuffer master(){
        return alloc("master");
    }

    public static DbBuffer alloc(String name){
        DbBuffer buffer = bufferCache.get(name);
        Assert.notNull(buffer, "not find buffer: " + name);
        return buffer;
    }

    public static DbBuffer alloc(DBConnection connection){
        String name = connection.getName();
        return bufferCache.computeIfAbsent(name, n -> {
            DBGlobalConfiguration configuration = new DBGlobalConfiguration();
            configuration.setDbConnection(connection);
            return new DbBuffer(configuration);
        });
    }

    public static Map<String, DbBuffer> getBufferCache() {
        return bufferCache;
    }

}
