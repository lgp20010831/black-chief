package com.black.utils;

import com.black.core.sql.code.log.Log;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class DynamicCachable implements Runnable{

    private volatile boolean close = false;

    private Log log = new CachableLog();

    //默认缓存存活时间为 5 分钟
    private long alive;

    private final Map<Object, CacheValue> cache = new ConcurrentHashMap<>();

    private Thread listenerThread;

    public DynamicCachable(boolean startThread){
        this(1000 * 60 * 5, startThread);
    }

    public DynamicCachable(long alive, boolean startThread){
        this.alive = alive;
        if (startThread){
            (listenerThread = new Thread(this)).start();
        }
    }

    public Object get(Object key, Function<Object, Object> function){
        if (close){
            throw new IllegalStateException("current is closed");
        }
        CacheValue cv;
        if (cache.containsKey(key)) {
            cv = cache.get(key);
        }else {
            Object value = function.apply(key);
            cv = new CacheValue(value, key);
            cache.put(key, cv);
        }
        cv.setHitTime(current());
        return cv.getValue();
    }


    @Override
    public void run() {
        while (!close){
            long current = current();
            long packTime = alive;
            CacheValue[] values = cache.values().toArray(new CacheValue[0]);
            for (int i = 0; i < values.length; i++) {
                CacheValue cacheValue = values[i];
                long hitTime = cacheValue.getHitTime();
                if (current > hitTime + alive){
                    release(cacheValue.getKey());
                }else {
                    packTime = Math.min(packTime, alive - (current - hitTime));
                }
            }
            pack(packTime);
        }
        cache.clear();
        listenerThread = null;
    }

    public boolean isClosed() {
        return close;
    }

    public void open(){
        close = false;
        if (listenerThread == null){
            listenerThread = new Thread(this);
            listenerThread.start();
        }
    }

    public boolean isOpen(){
        return !close && listenerThread != null;
    }

    public void close(){
        close = true;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    private long current(){
        return System.currentTimeMillis();
    }

    public long getAlive() {
        return alive;
    }

    public Thread getListenerThread() {
        return listenerThread;
    }

    private void pack(long time){
        if (time <= 5){
            return;
        }
        try {
            if (log.isInfoEnabled()) {
                log.info("pack: [" + time + "]");
            }
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }

    private void release(Object key){
        if (log.isInfoEnabled()) {
            log.info("缓存: ["+ key +"] 失效, 即将释放");
        }
        cache.remove(key);
    }

    @Getter @Setter
    private static class CacheValue{
        long hitTime;
        final Object value;
        final Object key;
        private CacheValue(Object value, Object key) {
            this.value = value;
            this.key = key;
        }
    }
}
