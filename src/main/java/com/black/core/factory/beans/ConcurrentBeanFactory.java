package com.black.core.factory.beans;

import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Deprecated
public class ConcurrentBeanFactory extends DefaultBeanFactory{


    private final Map<Class<?>, ReentrantLock> alikeTypeLockCache = new ConcurrentHashMap<>();

    @Override
    public <B> List<B> getBean(Class<B> genealogyClass, Map<String, Object> tempSource) {
        ReentrantLock lock;

        for (;;){
            /*
                According to the type to be created, obtain a lock from the
                buffer to ensure that the thread is serial when creating a
                bean of the same type
             */
              lock = alikeTypeLockCache.computeIfAbsent(genealogyClass, gc -> {
                    return new ReentrantLock();
                });

            ReentrantLock nowLock = alikeTypeLockCache.get(genealogyClass);
            if (nowLock != null && nowLock.equals(lock)){
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully obtained a lock, creating class: {}", genealogyClass.getSimpleName());
        }

        lock.lock();
        try {

            return super.getBean(genealogyClass, tempSource);
        }finally {
            if (lock.getQueueLength() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("In the process of creating an object, " +
                            "the waiting queue for this type of object is empty. " +
                            "Release the lock, creating class: {}", genealogyClass.getSimpleName());
                }
                alikeTypeLockCache.remove(genealogyClass);
            }
            lock.unlock();
        }
    }
}
