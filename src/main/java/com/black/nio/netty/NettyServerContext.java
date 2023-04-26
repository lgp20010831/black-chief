package com.black.nio.netty;

import com.black.core.log.IoLog;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class NettyServerContext extends NettyContext{

    private EventLoopGroup serverGroup;

    private ServerBootstrap bootstrap;

    private long startTime;

    public NettyServerContext(Configuration configuration) {
        super(configuration);
    }

    public boolean isClosed(){
        boolean nullAttr = serverGroup == null || ioEventGroup == null || bootstrap == null || channel == null;
        return nullAttr || close.get();
    }

    public void bind(){
        if (isClosed()){
            doBind();
        }
    }


    @Override
    public void close() {
        if (!isClosed()){
            Configuration configuration = getConfiguration();
            IoLog log = configuration.getLog();
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
            serverGroup.shutdownNow();
            ioEventGroup.shutdownNow();
            bootstrap = null;
            channel = null;
            serverGroup = null;
            ioEventGroup = null;
        }
    }

    private void doBind(){
        startTime = System.currentTimeMillis();
        Configuration configuration = getConfiguration();
        String host = configuration.getHost();
        int port = configuration.getPort();
        IoLog log = configuration.getLog();
        InetSocketAddress address = new InetSocketAddress(host, port);
        serverGroup = new NioEventLoopGroup(configuration.getServerThreadNum());
        ioEventGroup = new NioEventLoopGroup(configuration.getIoEventThreadNum());
        bootstrap = new ServerBootstrap();
        bootstrap.group(serverGroup, ioEventGroup).channel(NioServerSocketChannel.class);
        Consumer<AbstractBootstrap<?, ?>> bootstrapConsumer = configuration.getBootstrapConsumer();
        if (bootstrapConsumer != null){
            bootstrapConsumer.accept(bootstrap);
        }
        NettyServerContext context = this;
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {  // 绑定客户端连接时候触发操作
                    @Override
                    protected void initChannel(SocketChannel sh) throws Exception {
                        ChannelPipeline pipeline = sh.pipeline();
                        Consumer<ChannelPipeline> pipelineConsumer = configuration.getBeforePipelineConsumer();
                        if (pipelineConsumer != null){
                            pipelineConsumer.accept(pipeline);
                        }
                        pipeline.addFirst(ByteBufCaseToByteResolver.DEFAULT);
                        DispatchChannelHandler dispatchChannelHandler = new DispatchChannelHandler(context, sh);
                        Consumer<DispatchChannelHandler> dispatchCallback = configuration.getDispatchCallback();
                        if (dispatchCallback != null){
                            dispatchCallback.accept(dispatchChannelHandler);
                        }
                        pipeline.addLast(dispatchChannelHandler);
                        Consumer<ChannelPipeline> afterPipelineConsumer = configuration.getAfterPipelineConsumer();
                        if (afterPipelineConsumer != null){
                            afterPipelineConsumer.accept(pipeline);
                        }
                    }
                });
        ChannelFuture future = bootstrap.bind(address);
        channel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("server bind [{}]", address);
                    close.set(false);
                    log.info("server initialization completed in {} ms ", System.currentTimeMillis() - startTime);
                } else {
                    log.error(null, "server fail bind [{}]", address);
                    close();
                }
            }
        });

    }
}
