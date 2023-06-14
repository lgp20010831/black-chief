package com.black.thread;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class GroupTaskThreadPool<T> extends ThreadPoolExecutor {

    private final Map<T, AtomicInteger> counter = new ConcurrentHashMap<>();

    private final Map<T, Object> locks = new ConcurrentHashMap<>();

    private final Map<T, AtomicBoolean> openCutDown = new ConcurrentHashMap<>();

    public GroupTaskThreadPool(int coreSize){
        this(coreSize, coreSize * 2, 5, TimeUnit.HOURS, new LinkedBlockingQueue<>());
    }

    public GroupTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public GroupTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public GroupTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public GroupTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public void openCutdown(T group){
        AtomicBoolean down = openCutDown.get(group);
        if (down != null){
            if (down.compareAndSet(false, true)) {
                Object lock = locks.get(group);
                synchronized (lock){
                    lock.notifyAll();
                }
            }else {
                throw new IllegalStateException("Malicious modification of group: " + group);
            }
        }
    }

    public void executeGroupTask(Runnable runnable, T group){
        AtomicInteger count = counter.computeIfAbsent(group, g -> {
            AtomicInteger integer = new AtomicInteger(0);
            openCutDown.computeIfAbsent(g, c -> new AtomicBoolean(false));
            locks.computeIfAbsent(g, c -> new Object());
            return integer;
        });
        count.incrementAndGet();
        execute(wrapperTask(runnable, group));
    }


    protected Runnable wrapperTask(Runnable task, T group){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                }finally {
                    for (;;){
                        Object lock = locks.get(group);
                        AtomicInteger count = counter.get(group);
                        AtomicBoolean isOpen = openCutDown.get(group);
                        if (lock == null || count == null || openCutDown == null){
                            System.out.println("线程唤醒: " + Thread.currentThread().getName());
                            break;
                        }
                        int i = count.decrementAndGet();
                        if (isOpen.get() && i == 0){
                            System.out.println("最后指行者唤醒其他线程: " + Thread.currentThread().getName());
                            synchronized (lock){
                                //执行清理和唤醒工作
                                lock.notifyAll();
                                locks.remove(group);
                                counter.remove(group);
                                openCutDown.remove(group);
                            }

                            break;
                        }else {
                            try {
                                synchronized (lock){
                                    System.out.println("线程休眠: " + Thread.currentThread().getName());
                                    lock.wait();
                                }
                            } catch (InterruptedException e) {

                            }

                        }
                    }

                }
            }
        };
    }


    public static void main(String[] args) {
        GroupTaskThreadPool<Object> pool = new GroupTaskThreadPool<>(3);
        String group = "test";
        for (int i = 0; i < 3; i++) {
            pool.executeGroupTask(() -> {
                System.out.println("业务执行");
            }, group);
        }
        pool.openCutdown(group);
    }

}
