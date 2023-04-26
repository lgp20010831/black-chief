package com.black.core.cache;

import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public final class ClassSourceCache {

    public static boolean printRegisterSize = false;
    private static final Map<String, Set<Class<?>>> sourceCache = new ConcurrentHashMap<>();

    public static void registerSource(String path, Set<Class<?>> source){
        if (printRegisterSize && log.isInfoEnabled()) {
            log.info("path: {}, size:{}", path, source.size());
        }
        sourceCache.put(path, source);
    }

    public static Set<Class<?>> getSource(String path){
        return sourceCache.get(path);
    }

    public static Map<String, Set<Class<?>>> getSourceCache() {
        return sourceCache;
    }
}
