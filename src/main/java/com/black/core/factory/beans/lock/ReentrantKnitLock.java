package com.black.core.factory.beans.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 李桂鹏
 * @create 2023-05-24 13:37
 */
@SuppressWarnings("all")
public class ReentrantKnitLock implements KnitLock{

    private final ReentrantLock lock;

    public ReentrantKnitLock(boolean fair) {
        lock = new ReentrantLock(fair);
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
