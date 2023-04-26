package com.black.cache;


import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MapCacher implements Cacher{

    private final Map<Object, Object> map;

    public MapCacher(){
        this(new ConcurrentHashMap<>());
    }

    public MapCacher(@NonNull Map<Object, Object> map) {
        this.map = map;
    }

    @Override
    public Object getCache(Object key) {
        return map.get(key);
    }

    @Override
    public boolean exists(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Set<Object> keys() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Object rename(Object key, Object newkey) {
        if (exists(key)){
            return map.put(newkey, map.remove(key));
        }
        return null;
    }

    @Override
    public Class<?> type(Object key) {
        Object cache = getCache(key);
        return cache == null ? null : cache.getClass();
    }

    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object putWithExpire(Object key, Object value, TimeUnit unit, long time) {
        throw new UnsupportedOperationException("map cacher unsupport expire cache");
    }

    /**
     * 为缓存增加/减少过期时间
     *
     * @param key     缓存 key
     * @param unit    时间单位
     * @param time    时间值, 如果该值 <0 则为减少时间
     * @param requird 该值为 true 表示缓存中必须存在目标 key, 否则报错, false 则不会强制判断
     */
    @Override
    public void addExpire(Object key, TimeUnit unit, long time, boolean requird) {
        throw new UnsupportedOperationException("map cacher unsupport expire cache");
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public boolean clear() {
        map.clear();
        return true;
    }

    @Override
    public Map<Object, Object> toMap() {
        return new HashMap<>(map);
    }
}
