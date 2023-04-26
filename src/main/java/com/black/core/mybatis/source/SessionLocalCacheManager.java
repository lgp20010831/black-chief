package com.black.core.mybatis.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SessionLocalCacheManager {

    private static final ThreadLocal<List<ClearCacheWrapper>> localClearCache = new ThreadLocal<>();

    public static void register(Collection<String> alias, boolean clear){
        List<ClearCacheWrapper> clearCacheWrappers = localClearCache.get();
        if (clearCacheWrappers == null){
            localClearCache.set(clearCacheWrappers = new ArrayList<>());
        }
        clearCacheWrappers.add(buildWrapper(alias, clear));
    }

    public static void remove(){
        localClearCache.remove();
    }

    public static boolean needClearCache(String alias){
        List<ClearCacheWrapper> clearCacheWrappers = localClearCache.get();
        if (clearCacheWrappers != null){
            for (ClearCacheWrapper wrapper : clearCacheWrappers) {
                if (wrapper.getAliases().contains(alias)) {
                    return wrapper.isClear();
                }
            }
        }
        return false;
    }

    private static ClearCacheWrapper buildWrapper(Collection<String> alias, boolean clear){
        return new ClearCacheWrapper(alias, clear);
    }
}
