package com.black.thread;

import com.black.core.spring.util.ApplicationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
}
