package com.black.nio.code;

import com.black.nio.code.buf.PoolByteBufferGroup;

import java.io.IOException;

public interface NioContext {

    //获取配置类
    Configuration getConfiguration();

    //获取事件线程组
    EventLoopGroup getEventLoopGroup();

    //获取缓冲区组
    PoolByteBufferGroup getBufferGroup();

    //关闭
    void shutdown();

    void start() throws IOException;

    void shutdownNow();
}
