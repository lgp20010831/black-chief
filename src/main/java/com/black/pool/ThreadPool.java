package com.black.pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class ThreadPool extends AbstractIdlePool<Thread>{

    private ThreadFactory threadFactory;

    private AtomicInteger count = new AtomicInteger(0);

    public ThreadPool(Configuration configuration) {
        super(configuration);
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    @Override
    protected Thread create0() throws Throwable {
        Thread thread;
        if (threadFactory == null){
            thread = new Thread("pool-" + count.incrementAndGet());
        }else {
            thread = threadFactory.newThread(new Worker());
        }
        return null;
    }

    class Worker implements Runnable{

        @Override
        public void run() {
            Thread currentThread = Thread.currentThread();
            while (!Thread.interrupted()){

            }
        }
    }
}
