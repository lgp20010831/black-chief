package com.black.standard;

import com.black.core.cache.TypeConvertCache;

@SuppressWarnings("all")
public interface TypeConvertStandard {

    default <T> T convert(Object value, Class<T> type){
        return TypeConvertCache.initAndGet().genericConvert(type, value);
    }

}
