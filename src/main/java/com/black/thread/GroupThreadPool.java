package com.black.thread;

import com.black.pool.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author 李桂鹏
 * @create 2023-06-13 10:51
 */
@SuppressWarnings("all")
public class GroupThreadPool extends ThreadPool{


    public GroupThreadPool(int coreSize) {
        super(coreSize);
    }

    public GroupThreadPool(int coreSize, int maxSize) {
        super(coreSize, maxSize);
    }

    public GroupThreadPool(int coreSize, int maxSize, long keepAlive, TimeUnit unit) {
        super(coreSize, maxSize, keepAlive, unit);
    }

    public GroupThreadPool(Configuration configuration) {
        super(configuration);
    }


}
