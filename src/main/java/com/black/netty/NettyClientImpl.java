package com.black.netty;


import com.black.netty.branch.*;
import com.black.netty.ill.SessionException;
import com.black.netty.ill.WrongOperationException;
import com.black.core.util.CentralizedExceptionHandling;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Log4j2  @SuppressWarnings("all")
public class NettyClientImpl implements IoImpl {
    private final Configuration configuration;
    private final SocketAddress address;
    private NettySession nettySession;
    private ChannelFuture closeChannlFuture;

    public NettyClientImpl(Configuration configuration) {
        this.configuration = configuration;
        final String ip = configuration.getIp();
        final int port = configuration.getPort();
        if (port < 0 || port > 65535){
            throw new SessionException("port should is 0 - 65535");
        }
        if (ip == null){
            address = new InetSocketAddress(port);
        }else {
            address = new InetSocketAddress(ip, port);
        }
    }

    public NettySession start() throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new ClientHandler(configuration));

                    }
                });
        final ChannelFuture future = b.connect(address);
        Channel channel = future.channel();
        nettySession = new NettySessionImpl(channel, this, configuration);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture arg0) throws Exception {
                if (future.isSuccess()) {
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("connect successful");
                    }
                    StartComplete startComplete = configuration.getStartComplete();
                    if (startComplete != null){
                        startComplete.handlerStartComplete(nettySession);
                    }
                } else {
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("connect fail");
                    }
                    CentralizedExceptionHandling.handlerException(future.cause());
                    group.shutdownGracefully(); //关闭线程组
                }
            }
        });

        closeChannlFuture = channel.closeFuture();
        closeChannlFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("close client");
                    }
                    CloseSettlement closeSettlement = configuration.getCloseSettlement();
                    if (closeSettlement != null){
                        closeSettlement.handlerCloseSettlement(nettySession);
                    }
                    group.shutdownGracefully();
                }else if (future.isCancelled()){
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("will close client is cancel");
                    }
                }
            }
        });
        return nettySession;
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public NettySession bind() {
        throw new WrongOperationException("this is client");
    }

    @Override
    public NettySession connect() {
        try {
            return start();
        } catch (Exception e) {
            throw new SessionException(e);
        }
    }

    @AllArgsConstructor
    public static class ClientHandler extends SimpleChannelInboundHandler<String>{

        private final Configuration configuration;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            ReadMessage readMessage = configuration.getReadMessage();
            if (readMessage != null){
                readMessage.handlerReadMessage(new NettyContextBoard(ctx), msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ReadComplete readComplete = configuration.getReadComplete();
            if (readComplete != null){
                readComplete.handlerReadComplete(new NettyContextBoard(ctx));
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ConnectComplete connectComplete = configuration.getConnectComplete();
            if (connectComplete != null){
                connectComplete.handlerConnectComplete(new NettyContextBoard(ctx));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ThrowableCaught throwableCaught = configuration.getThrowableCaught();
            if (throwableCaught != null){
                throwableCaught.handlerThrowableCaught(new NettyContextBoard(ctx), cause);
            }else {
                ctx.close();
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            LossConnection lossConnection = configuration.getLossConnection();
            if (lossConnection != null){
                lossConnection.handlerLossConnection(new NettyContextBoard(ctx));
            }
        }
    }

}
