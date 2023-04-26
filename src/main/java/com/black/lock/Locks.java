package com.black.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Locks {

    private static final Map<Object, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public static ReentrantLock findLock(Object obj){
        return lockMap.computeIfAbsent(obj, o -> new ReentrantLock());
    }

    public static void lock(Object obj){
        if (obj == null) return;
        ReentrantLock lock = findLock(obj);
        lock.lock();
    }

    public static void unlock(Object obj){
        if (obj == null) return;
        ReentrantLock lock = findLock(obj);
        lock.unlock();
    }

    public static void lockRun(Object obj, Runnable runnable){
        lock(obj);
        try {
            runnable.run();
        }finally {
            unlock(obj);
        }
    }
}
