package com.black.user;

public class CacheObject<V> {

    private final V v;

    private boolean lock;

    public CacheObject(V v) {
        this.v = v;
    }

    public V get(){
        return v;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return lock;
    }
}
