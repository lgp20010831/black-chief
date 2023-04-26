package com.black.nio.code;

import com.black.nio.code.buf.PoolByteBufferGroup;
import lombok.NonNull;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

public abstract class AbstractNioContext implements NioContext{

    private final Configuration configuration;

    protected EventLoopGroup eventLoopGroup;

    protected final PoolByteBufferGroup bufferGroup;

    //等待关闭线程
    protected Thread closeWaitThread;

    public AbstractNioContext(@NonNull Configuration configuration){
        this.configuration = configuration;

        //保持与 配置类的相互关联
        configuration.setContext(this);

        //每个 context 维护一个 buffer 组
        bufferGroup = new PoolByteBufferGroup(configuration.getBufferGroupSize());

        //注册关闭钩子
        NioShutdownThreadHook hook = NioShutdownThreadHook.getInstance();
        hook.addContext(this);
        hook.registerHook();
    }

    @Override
    public PoolByteBufferGroup getBufferGroup() {
        return bufferGroup;
    }

    protected abstract EventLoopGroup createEventLoopGroup() throws IOException;

    @Override
    public void shutdown() {

        park();
        //自主关闭的依据, 当当前 channel 已经失效
        EventLoopGroup eventLoopGroup = getEventLoopGroup();
        if (eventLoopGroup != null){
            eventLoopGroup.close();
        }
    }


    void park(){
        if (closeWaitThread != null){
            return;
        }
        //挂起线程, 等待唤醒
        closeWaitThread = Thread.currentThread();
        LockSupport.park();
        closeWaitThread = null;
    }

    @Override
    public void shutdownNow() {
        EventLoopGroup eventLoopGroup = getEventLoopGroup();
        if (eventLoopGroup != null){
            eventLoopGroup.close();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }
}
