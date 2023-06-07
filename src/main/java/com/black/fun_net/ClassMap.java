package com.black.fun_net;

import java.util.LinkedHashMap;

@SuppressWarnings("all")
public class ClassMap<V> extends LinkedHashMap<Class<?>, V> {

    @Override
    public V get(Object key) {
        Class<?> findKey = (Class<?>) key;
        if (Object.class.equals(findKey)){
            return super.get(Object.class);
        }
        V result = null;
        while (findKey != null && !Object.class.equals(findKey)){
            V v = super.get(findKey);
            if (v != null){
                result = v;
                break;
            }
            findKey = findKey.getSuperclass();
        }
        return result;
    }
}
