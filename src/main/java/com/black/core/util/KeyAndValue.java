package com.black.core.util;

import com.black.netty.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KeyAndValue<K, V> {

    private final K key;

    private final V value;

    public static <R, W> List<KeyAndValue<R, W>> apart(Map<R, W> map){
        if (map == null || map.isEmpty()){
            return new ArrayList<>();
        }
        List<KeyAndValue<R, W>> list = new ArrayList<>();
        map.forEach((k, v) -> list.add(new KeyAndValue<>(k, v)));
        return list;
    }

    public static <K, V> KeyAndValue<K, V> get(K key, V val){
        return new KeyAndValue<>(key, val);
    }

    public KeyAndValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public Map<K, V> asConcurrentMap(){
        return asMap(new ConcurrentHashMap<>());
    }

    public Map<K, V> asMap(){
        return asMap(new HashMap<>());
    }

    public Map<K, V> asMap(Map<K, V> map){
        map.put(getKey(), getValue());
        return map;
    }

    public Session openSession(){
        return new KeyValueSession();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyAndValue<?, ?> that = (KeyAndValue<?, ?>) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    public static class KeyValueSession implements Session{

        @Override
        public void write(String message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeAndFlush(String message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    }
}
