package com.black.user;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<K, V> {

    private final Map<K, CacheObject<V>> cache = new ConcurrentHashMap<>();

    private final Set<K> blacklist = new HashSet<>();

    private final Set<K> waitlist = new HashSet<>();

    public Cache<K, V> push(K k, V v){
        if (blacklist.contains(k)){
            throw new CachesException(k + " is in blacklist");
        }
        cache.put(k, new CacheObject<>(v));
        return this;
    }

    public void wait(K k){
        synchronized (waitlist){
            waitlist.add(k);
        }
    }

    public void awaken(K k){
        synchronized (waitlist){
            waitlist.remove(k);
        }
    }

    public V get(K k){

        for (;;){
            if (!waitlist.contains(k)){
                break;
            }
        }
        CacheObject<V> co = cache.get(k);
        for (;;){
            if (co == null || !co.isLock())
                break;
        }
        return co == null ? null : co.get();
    }

    public void clear(){
        cache.clear();
        blacklist.clear();
        waitlist.clear();
    }

    public boolean unlock(K k){
        CacheObject<V> co = cache.get(k);
        if (co != null && co.isLock()){
            co.setLock(false);
            return true;
        }
        return false;
    }

    public boolean lock(K k){
        CacheObject<V> co = cache.get(k);
        if (co != null){
            co.setLock(true);
            return true;
        }
        return false;
    }

    public V remove(K k){
        CacheObject<V> co = cache.remove(k);
        return co == null ? null : co.get();
    }

    public Cache<K, V> addBlack(K k){
        synchronized (blacklist){
            blacklist.add(k);
        }
        return this;
    }

    public Cache<K, V> removeBlack(K k){
        synchronized (blacklist){
            blacklist.remove(k);
        }
        return this;
    }
}
