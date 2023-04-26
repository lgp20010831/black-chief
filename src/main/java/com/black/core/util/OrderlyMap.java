package com.black.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class OrderlyMap<K, V> implements Map<K, V> {

    final Map<K, V> map;

    final List<K> sort = new ArrayList<>();

    public OrderlyMap() {
        this(null);
    }

    public OrderlyMap(Map<K, V> map0){
        map = createMap();
        if (map0 != null){
            putAll(map0);
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public List<V> listValues(){
        List<V> list = new ArrayList<>();
        synchronized (sort){
            for (K k : sort) {
                list.add(map.get(k));
            }
        }
        return list;
    }

    public List<K> listKey(){
        return new ArrayList<>(sort);
    }

    public boolean isSingle(){
        return size() == 1;
    }

    public V firstElement(){
        if (sort.isEmpty()){
            return null;
        }
        return map.get(sort.get(0));
    }

    public V lastElement(){
        if (sort.isEmpty()){
            return null;
        }
        return map.get(sort.get(sort.size() - 1));
    }

    public OrderlyMap<K, V> filter(BiPredicate<K, V> predicate){
        return filter(predicate, false);
    }

    public OrderlyMap<K, V> filter(BiPredicate<K, V> predicate, boolean remove){
        OrderlyMap<K, V> filterMap = new OrderlyMap<>();
        synchronized (sort){
            for (K k : sort) {
                V v = map.get(k);
                if (predicate.test(k, v)) {
                    filterMap.put(k, v);
                    if (remove){
                        map.remove(k);
                    }
                }
            }
        }
        return filterMap;
    }

    public K firstKey(){
        return sort.isEmpty() ? null : sort.get(0);
    }

    public K lastKey(){
        return sort.isEmpty() ? null : sort.get(sort.size() - 1);
    }

    protected Map<K, V> createMap(){
        return new ConcurrentHashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        synchronized (sort){
            sort.remove(key);
            sort.add(key);
        }
        V v = map.put(key, value);
        return v;
    }

    @Override
    public V remove(Object key) {
        V remove = map.remove(key);
        synchronized (sort){
            sort.remove(key);
        }
        return remove;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        map.putAll(m);
        synchronized (sort){
            sort.addAll(m.keySet());
        }
    }

    @Override
    public void clear() {
        map.clear();
        sort.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        synchronized (sort){
            for (K k : sort) {
                V v = get(k);
                action.accept(k, v);
            }
        }
    }
}
