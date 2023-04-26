package com.black.pool;

import java.util.concurrent.TimeUnit;

public class Configuration {

    //核心连接数
    private int corePoolSize = 10;

    //最大可以创建的连接数, 如果设置 -1, 则无限制创建连接
    private int maxPoolSize = 20;

    //闲置的 socket 连接销毁前空闲时间
    private long unusedSocketKeepAlive = 0;

    //闲置的 socket 连接销毁的时间单位
    private TimeUnit unusedSocketTimeUnit = TimeUnit.SECONDS;

    //休眠等待连接空闲时间
    private long waitForIdleTime = 10 * 1000;

    private boolean lazyInit = false;

    private PoolElementFactory poolElementFactory;

    public Configuration(){

    }

    public void setPoolElementFactory(PoolElementFactory poolElementFactory) {
        this.poolElementFactory = poolElementFactory;
    }

    public PoolElementFactory getPoolElementFactory() {
        return poolElementFactory;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public long getUnusedSocketKeepAlive() {
        return unusedSocketKeepAlive;
    }

    public void setUnusedSocketKeepAlive(long unusedSocketKeepAlive) {
        this.unusedSocketKeepAlive = unusedSocketKeepAlive;
    }

    public TimeUnit getUnusedSocketTimeUnit() {
        return unusedSocketTimeUnit;
    }

    public void setUnusedSocketTimeUnit(TimeUnit unusedSocketTimeUnit) {
        this.unusedSocketTimeUnit = unusedSocketTimeUnit;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public void setWaitForIdleTime(long waitForIdleTime) {
        this.waitForIdleTime = waitForIdleTime;
    }

    public long getWaitForIdleTime() {
        return waitForIdleTime;
    }

}
