package com.black.thread;

import com.black.function.Consumer;
import com.black.function.Function;
import com.black.function.Runnable;

import java.util.Collection;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-05-24 9:41
 */
@SuppressWarnings("all")
public interface ThreadHandler {

    Executor getExecutor();

    void setExecutor(Executor executor);

    void setNamePrefix(String namePrefix);

    void setOpenReckonTime(boolean openReckonTime);

    void runSingleThread(Runnable runnable);

    void runThreads(Runnable runnable, int size);

    <S> void runThreads(Consumer<S> consumer, Collection<S> sources);

    void latch(Runnable runnable, int size);

    <S> void latch(Consumer<S> consumer, Collection<S> sources);

    void cyclic(Runnable runnable, int size, Runnable end);

    <S> void cyclic(Consumer<S> consumer, Collection<S> sources, Runnable end);

    <S, T> void cyclic(Function<S, T> function, Collection<S> sources, Consumer<Collection<T>> consumer);

    void semaphore(Runnable runnable, int size, int resources);

    <S> void semaphore(Consumer<S> consumer, Collection<S> sources, int resources);

    void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler);
}
