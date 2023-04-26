package com.black.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractVariousMap<V> {

    protected Map<String, V> map;

    public AbstractVariousMap(){
        map = createMap();
        if (map == null){
            throw new IllegalStateException("MAP IS NULL");
        }
    }

    protected Map<String, V> createMap(){
        return new ConcurrentHashMap<>();
    }

    public V get(String key){
        return map.get(key);
    }

    public boolean contain(String key){
        return map.containsKey(key);
    }

    public AbstractVariousMap<V> put(String key, V val){
        map.put(key, val);
        return this;
    }

    public AbstractVariousMap<V> clear(){
        map.clear();
        return this;
    }

    public Map<String, V> getMap(){
        return map;
    }
}
