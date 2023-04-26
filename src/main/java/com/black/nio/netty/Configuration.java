package com.black.nio.netty;

import com.black.nio.code.WorkThreadFactory;
import com.black.core.asyn.AsynConfiguration;
import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.*;
import java.util.function.Consumer;

@Getter @Setter
public class Configuration {

    //服务端接收连接轮询数
    private int serverThreadNum = 1;

    private int ioEventThreadNum = 10;

    private String host = "0.0.0.0";

    private int port = 6666;

    //bootstrap call back
    private Consumer<AbstractBootstrap<?, ?>> bootstrapConsumer;

    private Consumer<ChannelPipeline> beforePipelineConsumer;

    private Consumer<ChannelPipeline> afterPipelineConsumer;

    private Consumer<DispatchChannelHandler> dispatchCallback;

    private IoLog log = new CommonLog4jLog();

    private boolean openWorkPool = false;

    private ThreadPoolExecutor workPool;

    private int workPoolCoreSize = 10;

    private long keepAliveTime = 5;

    private TimeUnit workPoolUnit = TimeUnit.MINUTES;

    private int workBlockQueueSize = -1;

    public void initWorkPool(){
        if (workPool == null){
            workPool = new ThreadPoolExecutor(workPoolCoreSize,
                    workPoolCoreSize * 2,
                    keepAliveTime,
                    workPoolUnit,
                    workBlockQueueSize == -1 ? new LinkedBlockingDeque<>() : new ArrayBlockingQueue<>(workBlockQueueSize),
                    new WorkThreadFactory(),
                    rejectedExecutionHandler);
        }
    }

    private RejectedExecutionHandler rejectedExecutionHandler = new AsynConfiguration.ACallerRunsPolicy();
}
