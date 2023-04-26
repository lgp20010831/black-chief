package com.black.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalMap<K, V> extends AbstractLocal<Map<K, V>> implements Map<K, V>{


    @Override
    public int size() {
        return current().size();
    }

    @Override
    public boolean isEmpty() {
        return current().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return current().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return current().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return current().get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return current().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return current().remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        current().putAll(m);
    }

    @Override
    public void clear() {
        current().clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return current().keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return current().values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return current().entrySet();
    }


    @Override
    Map<K, V> create() {
        return new HashMap<>();
    }
}
