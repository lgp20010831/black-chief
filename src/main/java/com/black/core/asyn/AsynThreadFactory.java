package com.black.core.asyn;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynThreadFactory implements ThreadFactory {

    public static final String ALIAS = "asyn-task-";

    private final AtomicInteger size = new AtomicInteger(0);

    @Override
    public Thread newThread(@NotNull Runnable r) {
        size.incrementAndGet();
        return new Thread(r, ALIAS + size.get());
    }

    @Override
    public String toString() {
        return "asyn-thread-facotry";
    }
}
