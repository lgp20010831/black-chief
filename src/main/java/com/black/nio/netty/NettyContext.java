package com.black.nio.netty;

import com.black.function.Runnable;
import com.black.nio.code.AttysNioException;
import com.black.core.log.IoLog;
import io.netty.channel.*;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("all")
public abstract class NettyContext {

    protected final Configuration configuration;

    protected EventLoopGroup ioEventGroup;

    protected Channel channel;

    protected final AtomicBoolean close = new AtomicBoolean(true);

    public NettyContext(Configuration configuration) {
        this.configuration = configuration;
        NettyContextShutdownRunnable.registerShutDownContext(this);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean isClosed(){
        boolean nullAttr = ioEventGroup == null || channel == null;
        return nullAttr || close.get();
    }

    public EventLoopGroup getIoEventGroup() {
        return ioEventGroup;
    }

    public Channel channel(){
        return channel;
    }

    public void execute(Runnable runnable){
        execute(null, runnable);
    }

    public IoLog getLog(){
        return getConfiguration().getLog();
    }

    public void execute(ChannelHandlerContext ctx, Runnable runnable){
        Configuration configuration = getConfiguration();
        if (!configuration.isOpenWorkPool()) {
            throw new IllegalArgumentException("work is not allow use");
        }
        configuration.initWorkPool();
        ThreadPoolExecutor workPool = configuration.getWorkPool();
        workPool.execute(() -> {
            try {

                runnable.run();
            } catch (Throwable e) {
                if (ctx != null){
                    ctx.fireExceptionCaught(e);
                }else {
                    throw new AttysNioException(e);
                }
            }
        });
    }

    public void close(){
        if (!isClosed()){
            Configuration configuration = getConfiguration();
            IoLog log = configuration.getLog();
            ioEventGroup.shutdownNow();
            ChannelFuture future = channel.close();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()){
                        log.debug("server close successful");
                        close.set(true);
                        clear();
                    }
                }
            });
        }
    }

    protected void clear(){
        if (isClosed()){
            channel = null;
            ioEventGroup = null;
        }
    }

}
