package com.black.utils;


import com.black.core.util.Av0;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class FixMap<K, V> implements Map<K, V> {

    private volatile K k1;

    private volatile V v1;

    private volatile K k2;

    private volatile V v2;

    public static <K, V> FixMap<K, V> ofMap(){
        return ofMap(null, null);
    }

    public static <K, V> FixMap<K, V> ofMap(K k1, V v1){
        return ofMap(k1, v1, null, null);
    }

    public static <K, V> FixMap<K, V> ofMap(K k1, V v1, K k2, V v2){
        FixMap<K, V> map = new FixMap<>();
        if (k1 != null){
            map.put(k1, v1);
        }
        if (k2 != null){
            map.put(k2, v2);
        }
        return map;
    }

    @Override
    public int size() {
        return isEmpty() ? 0 : (k1 != null && k2 != null) ? 2 : 1;
    }

    @Override
    public boolean isEmpty() {
        return k1 == null && k2 == null;
    }

    @Override
    public boolean containsKey(Object key) {
        return key.equals(k1) || key.equals(k2);
    }

    @Override
    public boolean containsValue(Object value) {
        return value != null && (value.equals(v1) || value.equals(v2));
    }

    @Override
    public V get(Object key) {
        return key.equals(k1) ? v1 : (key.equals(k2) ? v2 : null);
    }

    @Nullable
    @Override
    public V put(@NotNull K key, V value) {
        if (k1 == null || key.equals(k1)){
            k1 = key;
            v1 = value;
        }else if (k2 == null || key.equals(k2)){
            k2 = key;
            v2 = value;
        }else {
            throw new IllegalStateException("map is the upper limit is reached");
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        V r = null;
        if (key.equals(k1)){
            r = v1;
            k1 = null;
            v1 = null;
        }else if (key.equals(k2)){
            r = v2;
            k2 = null;
            v2 = null;
        }
        return r;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        if (m.size() > 2){
            throw new IllegalStateException("map is the upper limit is reached");
        }
        for (K k : m.keySet()) {
            V v = m.get(k);
            put(k, v);
        }
    }

    @Override
    public void clear() {
        k1 = null;
        v1 = null;
        k2 = null;
        v2 = null;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return Av0.set(k1, k2);
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return Av0.as(v1, v2);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new IllegalStateException("current map is not support entry");
    }

    @Override
    public String toString() {
        StringJoiner builder = new StringJoiner(",", "[", "]");
        if (k1 != null){
            builder.add(k1 + "=" + v1);
        }
        if (k2 != null){
            builder.add(k2 + "=" + v2);
        }
        return builder.toString();
    }
}
