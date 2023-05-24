package com.black.thread;

import com.black.core.asyn.AsynConfigurationManager;
import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Utils;
import com.black.function.Consumer;
import com.black.function.Function;
import com.black.function.Runnable;
import lombok.Getter;


import java.util.Collection;
import java.util.concurrent.*;

/**
 * @author 李桂鹏
 * @create 2023-05-24 10:02
 */
@SuppressWarnings("all") @Getter
public class AsyncThreadHandler implements ThreadHandler{

    public static final AsyncThreadHandler GLOBAL = new AsyncThreadHandler();

    private boolean openReckonTime = false;

    @Override
    public void setNamePrefix(String namePrefix) {
        //async can not set prefix
    }

    @Override
    public void setOpenReckonTime(boolean openReckonTime) {
        this.openReckonTime = openReckonTime;
    }

    protected void runBlackTask(Runnable runnable){
        runBlackTask(runnable, true);
    }

    protected void runBlackTask(Runnable runnable, boolean wrapper){

        if (!wrapper){
            try {
                runnable.run();
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            return;
        }

        try {
            if (beforeRun()) {
                runnable.run();
            }
        } catch (Throwable e) {
            postTaskFail(e);
            throw new IllegalStateException(e);
        }finally {
            afterRun();
        }
    }

    protected <S> void runBlackConsumer(Consumer<S> consumer, S source){
        runBlackConsumer(consumer, source, true);
    }

    protected <S> void runBlackConsumer(Consumer<S> consumer, S source, boolean wrapper){

        if (!wrapper){
            try {
                consumer.accept(source);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            return;
        }

        try {
            if (beforeRun()) {
                consumer.accept(source);
            }
        } catch (Throwable e) {
            postTaskFail(e);
            throw new IllegalStateException(e);
        }finally {
            afterRun();
        }
    }

    protected <S, T> T runBlackFunnction(Function<S, T> function, S source){
        return runBlackFunnction(function, source, true);
    }

    protected <S, T> T runBlackFunnction(Function<S, T> function, S source, boolean wrapper){

        if (!wrapper){
            try {
                return function.apply(source);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }


        try {
            if (beforeRun()) {
                return function.apply(source);
            }
            return null;
        } catch (Throwable e) {
            postTaskFail(e);
            throw new IllegalStateException(e);
        }finally {
            afterRun();
        }
    }

    protected boolean beforeRun(){
        return true;
    }

    protected void afterRun(){

    }

    protected void postTaskFail(Throwable ex){

    }

    protected void postLatchFinish(){

    }

    protected void postLatchIntercept(){

    }

    protected void postCyclicFinish(){

    }

    protected void postCyclicThrowable(Throwable e){

    }

    @Override
    public void runSingleThread(Runnable runnable) {
        runSingleThread0(runnable, true);
    }

    protected void runSingleThread0(Runnable runnable, boolean wrapper){
        AsynGlobalExecutor.execute(new java.lang.Runnable() {
            @Override
            public void run() {
                if (isOpenReckonTime()){
                    ApplicationUtil.programRunMills(() -> {
                        runBlackTask(runnable, wrapper);
                    });
                }else {
                    runBlackTask(runnable, wrapper);
                }
            }
        });
    }

    @Override
    public void runThreads(Runnable runnable, int size) {
        for (int i = 0; i < size; i++) {
            runSingleThread(runnable);
        }
    }

    protected void runThreads(Runnable runnable, int size, boolean wrapper) {
        for (int i = 0; i < size; i++) {
            runSingleThread0(runnable, wrapper);
        }
    }

    protected <S> void runConsumer(Consumer<S> consumer, S source, boolean wrapper){
        AsynGlobalExecutor.execute(new java.lang.Runnable() {
            @Override
            public void run() {
                if (isOpenReckonTime()){
                    ApplicationUtil.programRunMills(() -> {
                        runBlackConsumer(consumer, source, wrapper);
                    });
                }else {
                    runBlackConsumer(consumer, source, wrapper);
                }
            }
        });
    }

    @Override
    public <S> void runThreads(Consumer<S> consumer, Collection<S> sources) {
        runThreads(consumer, sources, true);
    }

    public <S> void runThreads(Consumer<S> consumer, Collection<S> sources, boolean wrapper) {
        if (!Utils.isEmpty(sources)){
            for (S source : sources) {
                runConsumer(consumer, source, wrapper);
            }
        }
    }

    @Override
    public void latch(Runnable runnable, int size) {
        CountDownLatch latch = new CountDownLatch(size);
        runThreads(() -> {
            try {
                runBlackTask(runnable);
            }finally {
                System.out.println("减一次数");
                latch.countDown();
            }
        }, size, false);
        try {

            latch.await();
            postLatchFinish();
        } catch (InterruptedException e) {
            postLatchIntercept();
        }
    }

    @Override
    public <S> void latch(Consumer<S> consumer, Collection<S> sources) {
        if (!Utils.isEmpty(sources)){
            CountDownLatch latch = new CountDownLatch(sources.size());
            runThreads(ts -> {
                try {
                    runBlackConsumer(consumer, ts);
                }finally {
                    latch.countDown();
                }
            }, sources, false);
            try {
                latch.await();
                postLatchFinish();
            } catch (InterruptedException e) {
                postLatchIntercept();
            }
        }
    }

    @Override
    public void cyclic(Runnable runnable, int size, Runnable end) {
        CyclicBarrier barrier = new CyclicBarrier(size, new java.lang.Runnable() {
            @Override
            public void run() {
                try {
                    if (end != null){
                        runBlackTask(end);
                    }
                }finally {
                    postCyclicFinish();
                }

            }
        });
        runThreads(() -> {
            try {
                runBlackTask(runnable);
            }finally {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    postCyclicThrowable(e);
                    throw new IllegalStateException(e);
                }
            }
        }, size, false);
    }

    @Override
    public <S> void cyclic(Consumer<S> consumer, Collection<S> sources, Runnable end) {
        if (!Utils.isEmpty(sources)){
            CyclicBarrier barrier = new CyclicBarrier(sources.size(), new java.lang.Runnable() {
                @Override
                public void run() {
                    try {
                        if (end != null){
                            runBlackTask(end);
                        }
                    }finally {
                        postCyclicFinish();
                    }

                }
            });
            runThreads(ts -> {
                try {
                    runConsumer(consumer, ts, true);
                }finally {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        postCyclicThrowable(e);
                        throw new IllegalStateException(e);
                    }
                }
            }, sources, false);
        }
    }

    @Override
    public <S, T> void cyclic(Function<S, T> function, Collection<S> sources, Consumer<Collection<T>> consumer) {
        if (!Utils.isEmpty(sources)){
            LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
            CyclicBarrier barrier = new CyclicBarrier(sources.size(), new java.lang.Runnable() {
                @Override
                public void run() {
                    try {
                        if (consumer != null){
                            runBlackConsumer(consumer, queue);
                        }
                    }finally {
                        postCyclicFinish();
                    }

                }
            });
            runThreads(ts -> {
                try {
                    T t = runBlackFunnction(function, ts, true);
                    if (t != null){
                        queue.put(t);
                    }
                }finally {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        postCyclicThrowable(e);
                        throw new IllegalStateException(e);
                    }
                }
            }, sources, false);

        }
    }

    @Override
    public void semaphore(Runnable runnable, int size, int resources) {
        Semaphore semaphore = new Semaphore(resources);
        runThreads(() -> {
            try {
                semaphore.acquire();
                runBlackTask(runnable);
            } catch (InterruptedException e) {

            }finally {
                semaphore.release();
            }
        }, size);
    }

    @Override
    public <S> void semaphore(Consumer<S> consumer, Collection<S> sources, int resources) {
        if (!Utils.isEmpty(sources)){
            Semaphore semaphore = new Semaphore(resources);
            runThreads(ts -> {
                semaphore.acquire();
                try {
                    runConsumer(consumer, ts, true);
                }finally {
                    semaphore.release();
                }
            }, sources, false);
        }
    }

    @Override
    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        AsynConfigurationManager.getConfiguration().setExceptionHandler(exceptionHandler);
    }

}
