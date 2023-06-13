package com.black.thread;

/**
 * @author 李桂鹏
 * @create 2023-06-13 10:53
 */
@SuppressWarnings("all")
public class ThreadPoolExecutor implements Executor{

    private final ThreadPool pool;

    public ThreadPoolExecutor() {
        pool = new ThreadPool(8);
    }

    public ThreadPoolExecutor(ThreadPool pool){
        this.pool = pool;
    }

    @Override
    public void execute(Runnable task) {
        pool.execute(task);
    }
}
