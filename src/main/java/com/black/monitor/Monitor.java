package com.black.monitor;

import com.black.bin.ApplyProxyFactory;
import lombok.Data;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all") @Data
public class Monitor<K, V> {

    private final Map<K, V> origin;

    private final Map<K, ValueTimeWrapper<V>> activityCache;

    private Map<K, V> proxy;

    private TimeUnit unit;

    private long expire;

    public Monitor(@NonNull Map<K, V> origin) {
        this.origin = origin;
        activityCache = new ConcurrentHashMap<>();
    }


    protected synchronized Map<K, V> createProxy(){
        if (proxy != null){
            return proxy;
        }

        proxy = ApplyProxyFactory.proxy(origin, new MonitorApplyProxy<>(this));
        return proxy;
    }

    //进行检查, 并获取最近一个要过期元素的毫秒值
    protected long inspect(){
        long recently = -1;
        for (K key : new HashSet<>(activityCache.keySet())) {
            ValueTimeWrapper<V> timeWrapper = activityCache.get(key);
            long lastActivityTime = timeWrapper.getLastActivityTime();
            if (isExpire(lastActivityTime)){
                removeOrigin(key);
            }else {
                long clearance = getExpectedClearance(lastActivityTime);
                recently = recently == -1 ? clearance : Math.min(recently, clearance);
            }
        }
        return recently;
    }

    protected void removeOrigin(K key){
        origin.remove(key);
    }


    protected boolean isExpire(long lastActivityTime){
        long millis = getUnit().toMillis(getExpire());
        long now = System.currentTimeMillis();
        return now - lastActivityTime >= millis;
    }

    protected long getExpectedClearance(long lastActivityTime){
        long millis = getUnit().toMillis(getExpire());
        long now = System.currentTimeMillis();
        return now - lastActivityTime;
    }

}
