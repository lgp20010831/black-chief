package com.black.core.factory;


import com.black.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class CommonThreadFactory implements ThreadFactory, CommonFactory<Runnable, Thread> {

    private final String id;
    private final String name;
    private final AtomicInteger level = new AtomicInteger(0);

    public CommonThreadFactory(String name) {
        this.name = name;
        id = UUID.randomUUID().toString();
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        level.incrementAndGet();
        return new Thread(r, StringUtils.linkStr(name, "-", String.valueOf(level.get())));
    }

    @Override
    public Thread get(Runnable p) {
        return newThread(p);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Factory getParent() {
        return null;
    }
}
