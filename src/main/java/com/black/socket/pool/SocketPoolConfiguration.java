package com.black.socket.pool;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Setter @Getter @SuppressWarnings("all")
public class SocketPoolConfiguration {

    private String serverHost = "0.0.0.0";

    private int serverPort;

    //核心连接数
    private int corePoolSize = 10;

    //最大可以创建的连接数, 如果设置 -1, 则无限制创建连接
    private int maxPoolSize = 20;

    //闲置的 socket 连接销毁前空闲时间
    private long unusedSocketKeepAlive = 60;

    //闲置的 socket 连接销毁的时间单位
    private TimeUnit unusedSocketTimeUnit = TimeUnit.SECONDS;

    //休眠等待连接空闲时间
    private long waitForIdleTime = 10 * 1000;

    private boolean lazyInit = false;

    public SocketPoolConfiguration(int serverPort){
        this.serverPort = serverPort;
    }


    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
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
