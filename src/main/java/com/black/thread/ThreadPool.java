package com.black.thread;

import com.black.pool.AbstractIdlePool;
import com.black.pool.Closeable;
import com.black.pool.Configuration;
import com.black.pool.PoolElement;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author 李桂鹏
 * @create 2023-06-13 9:22
 */
@SuppressWarnings("all")
public class ThreadPool extends AbstractIdlePool<ThreadPool.Worker> {

    private final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    private final AtomicInteger count = new AtomicInteger(0);

    private boolean adequateResources = false;

    private ThreadFactory threadFactory;

    static Configuration makeConfig(int coreSize, int maxSize, long keepAlive, TimeUnit unit){
        Configuration configuration = new Configuration();
        configuration.setCorePoolSize(coreSize);
        configuration.setMaxPoolSize(maxSize);
        configuration.setLazyInit(true);
        configuration.setUnusedSocketKeepAlive(keepAlive);
        configuration.setUnusedSocketTimeUnit(unit);
        return configuration;
    }

    public ThreadPool(int coreSize){
        this(coreSize, -1);
    }

    public ThreadPool(int coreSize, int maxSize){
        this(coreSize, maxSize, 3, TimeUnit.MINUTES);
    }

    public ThreadPool(int coreSize, int maxSize, long keepAlive, TimeUnit unit){
        super(makeConfig(coreSize, maxSize, keepAlive, unit));
    }

    public ThreadPool(Configuration configuration) {
        super(configuration);
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setAdequateResources(boolean adequateResources) {
        this.adequateResources = adequateResources;
    }

    public boolean isAdequateResources() {
        return adequateResources;
    }

    @Override
    protected boolean structureInit() {
        return false;
    }

    @Override
    protected Worker create0() throws Throwable {
        Thread thread;
        Worker worker = new Worker();
        if (threadFactory != null){
            thread = threadFactory.newThread(worker);
        }else {
            thread = new Thread(worker, "pool-" + count.incrementAndGet());
        }
        thread.start();
        return worker;
    }

    public void execute(Runnable task){
        putTask(task);
        BlockingQueue<PoolElement<Worker>> coreSocketQueue = getCoreSocketQueue();
        boolean idle = false;
        for (PoolElement<Worker> element : coreSocketQueue) {
            if (!element.getOrigin().isRunning()){
                idle = true;
                element.getOrigin().unpark();
                break;
            }
        }

        if (!idle || adequateResources){
            getElement();
        }
    }

    protected void putTask(Runnable task){
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {

        }
    }

    public class Worker implements Closeable, Runnable {

        private volatile boolean wait;

        private boolean shutdown = false;

        private volatile boolean running = false;

        private Thread current;

        void park(){
            wait = true;
            LockSupport.park();
        }

        synchronized void unpark(){
            if (wait){
                wait = false;
                LockSupport.unpark(current);
            }
        }

        @Override
        public void close() throws Throwable {
            shutdown = true;
            unpark();
        }

        @Override
        public void run() {
            current = Thread.currentThread();
            while (!shutdown){
                Runnable task;
                if ((task = getTask()) != null){
                    runTask(task);
                }else if (taskQueue.isEmpty()){
                    park();
                }
            }
            System.out.println("thread shutdown: " + Thread.currentThread().getName());
        }

        public boolean isRunning() {
            return running;
        }

        void runTask(Runnable task){
            running = true;
            try {
                task.run();
            }finally {
                running = false;
            }
        }

        Runnable getTask(){
            return taskQueue.poll();
        }
    }

}
