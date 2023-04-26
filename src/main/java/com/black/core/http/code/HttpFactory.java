package com.black.core.http.code;

import java.util.HashMap;
import java.util.Map;

public class HttpFactory {

    static final Map<Class<?>, Object> cache = new HashMap<>();

    public static <M> M obtain(Class<M> clazz){
        return (M) cache.get(clazz);
    }

}
