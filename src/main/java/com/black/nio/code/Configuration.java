package com.black.nio.code;

import com.black.nio.code.buf.NioByteBuffer;
import com.black.nio.code.buf.bytebuffer.CommonByteBuffer;
import com.black.core.asyn.AsynConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.*;

@Setter @Getter
public class Configuration {

    private int port;

    private String host = "0.0.0.0";

    private int groupSize = 5;

    private int acceptBufferSize = 1024;

    private int bufferGroupSize = 1024 * 1024 * 16;

    private Class<? extends NioByteBuffer> bufferType = CommonByteBuffer.class;

    private NioContext context;

    private ChannelInitialization channelInitialization;

    private boolean writeInCurrentLoop = true;

    private boolean openWorkPool = false;

    private ThreadPoolExecutor workPool;

    private int workPoolCoreSize = 10;

    private long keepAliveTime = 5;

    private TimeUnit workPoolUnit = TimeUnit.MINUTES;

    private int workBlockQueueSize = -1;

    private RejectedExecutionHandler rejectedExecutionHandler = new AsynConfiguration.ACallerRunsPolicy();

    public Configuration(){
        this(5555);
    }

    public Configuration(int port) {
        this.port = port;
    }

    public Configuration(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public int getAcceptBufferSize() {
        if (acceptBufferSize > bufferGroupSize){
            acceptBufferSize = bufferGroupSize / 2;
        }
        return acceptBufferSize;
    }

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

    public SocketAddress getAddress(){
        return new InetSocketAddress(getHost(), getPort());
    }
}
