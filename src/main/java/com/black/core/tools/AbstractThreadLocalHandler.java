package com.black.core.tools;

import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;

public class AbstractThreadLocalHandler {

    protected static ThreadLocal<Object> local = new ThreadLocal<>();

    public static Object get(){
        return local.get();
    }

    public static <T> T get(Class<T> type){
        Object o = get();
        if (o == null){
            return null;
        }
        Class<?> lt = o.getClass();
        if (type.isAssignableFrom(lt)){
            return (T) o;
        }

        TypeHandler handler = TypeConvertCache.initAndGet();
        return (T) handler.convert(type, o);
    }
}
