package com.black.nio.code;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkThreadFactory implements ThreadFactory {

    public static final String ALIAS = "work-nio-";

    private final AtomicInteger size = new AtomicInteger(0);

    @Override
    public Thread newThread(@NotNull Runnable r) {
        size.incrementAndGet();
        return new Thread(r, ALIAS + size.get());
    }
}
