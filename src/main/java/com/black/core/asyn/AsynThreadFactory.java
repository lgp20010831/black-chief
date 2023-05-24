package com.black.core.asyn;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynThreadFactory implements ThreadFactory {

    public static final String ALIAS = "asyn-task-";

    private final AtomicInteger size = new AtomicInteger(0);

    @Override
    public Thread newThread(@NotNull Runnable r) {
        AsynConfiguration configuration = AsynConfigurationManager.getConfiguration();
        size.incrementAndGet();
        Thread thread = new Thread(r, ALIAS + size.get());
        thread.setUncaughtExceptionHandler(configuration.getExceptionHandler());
        return thread;
    }

    @Override
    public String toString() {
        return "asyn-thread-facotry";
    }
}
