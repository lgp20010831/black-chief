package com.black.thread;

import com.black.core.asyn.AsynGlobalExecutor;

/**
 * @author 李桂鹏
 * @create 2023-06-13 10:53
 */
@SuppressWarnings("all")
public class AsyncExecutor implements Executor{


    @Override
    public void execute(Runnable task) {
        AsynGlobalExecutor.execute(task);
    }
}
