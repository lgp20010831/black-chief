package com.black.monitor;

import com.black.core.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class MonitoringCreator {

    private static final Map<Object, Monitor<?, ?>> monitorCache = new ConcurrentHashMap<>();

    public static <K, V> Monitor<K, V> findMonitor(Map<K, V> proxy){
        return (Monitor<K, V>) Assert.nonNull(monitorCache.get(proxy));
    }

    //expire : 过期时间, 单位秒
    public static <K, V> Map<K, V> applyMonitor(Map<K, V> map, long expire){
        map = map == null ? new ConcurrentHashMap<>() : map;
        Monitor<K, V> monitor = new Monitor<>(map);
        monitor.setUnit(TimeUnit.SECONDS);
        monitor.setExpire(expire);
        Map<K, V> proxy = monitor.createProxy();
        monitorCache.put(proxy, monitor);
        return proxy;
    }


}
