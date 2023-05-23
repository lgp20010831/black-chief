package com.black.thread;

import com.black.core.spring.util.ApplicationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class ThreadUtils {

    public static final String THREAD_NAME_PREFIX = "chief-thread-";

    private static final AtomicInteger SORT = new AtomicInteger(0);


    public static String getName(){
        return getName(THREAD_NAME_PREFIX);
    }

    public static String getName(String prefix){
        return prefix + SORT.incrementAndGet();
    }

    public static List<Thread> runThreads(Runnable runnable, int size){
        return runThreads(runnable, size, false);
    }

    public static List<Thread> runThreads(Runnable runnable, int size, boolean reckonTime){
        ArrayList<Thread> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(runThread(runnable, reckonTime));
        }
        return list;
    }

    public static Thread runThread(Runnable runnable){
        return runThread(runnable, false);
    }

    public static Thread runThread(Runnable runnable, boolean reckonTime){
        Thread thread = new Thread(() -> {
            if (reckonTime) {
                ApplicationUtil.programRunMills(runnable);
            } else {
                runnable.run();
            }
        }, getName());
        thread.start();
        return thread;
    }

    public static void latch(Runnable runnable, int size){
        latch(runnable, size, false);
    }

    public static void latch(Runnable runnable, int size, boolean reckonTime){
        CountDownLatch latch = new CountDownLatch(size);
        runThreads(() -> {
            runnable.run();
            latch.countDown();
        }, size, reckonTime);
        try {

            latch.await();
        } catch (InterruptedException e) {

        }
    }

    public static void cyclic(Runnable runnable, int size, Runnable end){
        cyclic(runnable, size, end, false);
    }

    public static void cyclic(Runnable runnable, int size, Runnable end, boolean reckonTime){
        CyclicBarrier barrier = new CyclicBarrier(size, end);
        runThreads(() -> {
            runnable.run();
            try {
                barrier.await();
            } catch (InterruptedException e) {

            } catch (BrokenBarrierException e) {
                throw new IllegalStateException(e);
            }
        }, size, reckonTime);
    }

    public static void semaphoreLimit(Runnable runnable, int size, int resources){
        semaphoreLimit(runnable, size, resources, false);
    }

    public static void semaphoreLimit(Runnable runnable, int size, int resources, boolean reckonTime){
        Semaphore semaphore = new Semaphore(resources);
        runThreads(() -> {
            try {
                semaphore.acquire();
                runnable.run();
            } catch (InterruptedException e) {

            }finally {
                semaphore.release();
            }
        }, size, reckonTime);
    }
}
