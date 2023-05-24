package com.black.core.factory.beans.lock;

import java.util.concurrent.Semaphore;

/**
 * @author 李桂鹏
 * @create 2023-05-24 13:38
 */
@SuppressWarnings("all")
public class SemaphoreLock implements KnitLock{

    private final Semaphore semaphore;

    public SemaphoreLock(int size, boolean fair) {
        semaphore = new Semaphore(size, fair);
    }

    @Override
    public void lock() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void unlock() {
        semaphore.release();
    }
}
