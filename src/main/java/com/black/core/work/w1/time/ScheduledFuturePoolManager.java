package com.black.core.work.w1.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledFuturePoolManager {

    //核心线程池数量
    public static int poolSize = 4;
    public static final int MAX_POOL_SIZE = 20;
    public static final int MIN_POOL_SIZE = 1;
    private static ScheduledExecutorService executorService;

    public static ScheduledExecutorService getScheduledService(){
        if (executorService == null){
            if (poolSize < MIN_POOL_SIZE){
                poolSize = MIN_POOL_SIZE;
            }

            if (poolSize > MAX_POOL_SIZE){
                poolSize = MAX_POOL_SIZE;
            }
            executorService = Executors.newScheduledThreadPool(poolSize);
        }
        return executorService;
    }
}
