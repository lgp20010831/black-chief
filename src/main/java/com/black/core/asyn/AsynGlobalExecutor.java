package com.black.core.asyn;

import lombok.NonNull;

import java.util.concurrent.*;

public class AsynGlobalExecutor {

    private ThreadPoolExecutor pool;

    private ScheduledExecutorService executorService;

    private AsynGlobalExecutor(AsynConfiguration configuration){
        pool = new ThreadPoolExecutor(
                configuration.getCorePoolSize(),
                configuration.getMaximumPoolSize(),
                configuration.getKeepAliveTime(),
                configuration.getUnit(),
                configuration.getWorkQueue(),
                configuration.getThreadFactory(),
                configuration.getHandler()
        );

        executorService = new ScheduledThreadPoolExecutor(
                configuration.getTimeCorePoolSize(),
                configuration.getTimerThreadFactory(),
                configuration.getHandler()
        );
    }


    private static AsynGlobalExecutor asynGlobalExecutor;

    public static AsynGlobalExecutor getInstance(){
        AsynConfiguration configuration = AsynConfigurationManager.getConfiguration();
        return getInstance(configuration);
    }

    private static AsynGlobalExecutor getInstance(AsynConfiguration configuration){
        if (asynGlobalExecutor == null){
            asynGlobalExecutor = new AsynGlobalExecutor(configuration);
        }
        return asynGlobalExecutor;
    }

    public static void replacePool(ThreadPoolExecutor poolExecutor){
        getInstance().setPool(poolExecutor);
    }

    public static void executeNoThrowable(com.black.function.Runnable runnable){
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public static void execute(@NonNull Runnable runnable){
        getInstance().execute0(runnable);
    }


    public static ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit){
        return getInstance().getExecutorService().schedule(command, delay, unit);
    }

    public static <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, TimeUnit unit){
        return getInstance().getExecutorService().schedule(callable, delay, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit){
        return getInstance().getExecutorService().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit){
        return getInstance().getExecutorService().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public void execute0(@NonNull Runnable runnable){
        pool.execute(runnable);
    }

    @NonNull
    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    public ThreadPoolExecutor getPool() {
        return pool;
    }

    public static void shutdown(){
        getInstance().shutdownAll();
    }

    public void shutdownAll(){
        if (pool != null){
            pool.shutdown();
        }

        if (executorService != null){
            executorService.shutdown();
        }

    }
}
