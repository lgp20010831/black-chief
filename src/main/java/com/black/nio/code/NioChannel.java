package com.black.nio.code;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.code.buf.NioByteBuffer;
import com.black.nio.code.run.Future;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.ThreadPoolExecutor;

public interface NioChannel {

    //获取地址标识
    String nameAddress();

    //获取维护的 channel
    SelectableChannel channel();

    //获取绑定事件组
    EventLoop getEventLoop();

    //当前线程是否是绑定事件组的线程
    boolean inEventLoop();

    //获取配置类
    Configuration getConfiguration();

    //获取读缓冲区
    NioByteBuffer getReadByteBuffer();

    //获取写数据缓冲区
    NioByteBuffer getWriteByteBuffer();

    //关闭 channel
    void close() throws IOException;

    //冲刷数据
    void flush();

    void connectComplete();

    void acceptComplete();

    //传播异常, 基本都是 io 异常
    void error(Throwable e);

    //获取管道
    Pipeline getPipeline();

    //写数据
    void write(Object source);

    //写数据,并且冲刷数据
    void writeAndFlush(Object source);

    JHexByteArrayOutputStream getOutputStream();

    Future<?> addTaskInLoop(Runnable runnable);

    default Future<?> addTaskInRandomLoop(Runnable runnable){
        EventLoop selfLoop = getEventLoop();
        EventLoop loop;
        while ((loop = getEventLoop().getGroup().get()).equals(selfLoop)){

        }
        return loop.addTask(runnable);
    }

    default void executeWork(Runnable runnable){
        final Configuration configuration = getConfiguration();
        if (!configuration.isOpenWorkPool()) {
            throw new AttysNioException("工作池未被允许开启");
        }
        configuration.initWorkPool();
        ThreadPoolExecutor workPool = configuration.getWorkPool();
        workPool.execute(runnable);
    }
}
