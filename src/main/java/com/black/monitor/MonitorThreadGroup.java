package com.black.monitor;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

@SuppressWarnings("all") @Data
public class MonitorThreadGroup implements ThreadFactory {

    private static MonitorThreadGroup threadGroup;
    public static synchronized MonitorThreadGroup getInstance() {
        if (threadGroup == null){
            threadGroup = new MonitorThreadGroup();
        }
        return threadGroup;
    }
    private MonitorThreadGroup(){}

    private int maxMonitorOfSingleThread = 5;

    private final LinkedBlockingQueue<MonitorThread> threads = new LinkedBlockingQueue<>();

    @Override
    public Thread newThread(@NotNull Runnable r) {
        return null;
    }
}
