package com.black.netty;

import com.black.netty.branch.*;
import com.black.netty.ill.SessionException;
import com.black.netty.ill.WrongOperationException;
import com.black.core.util.CentralizedExceptionHandling;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Log4j2  @SuppressWarnings("all")
public final class NettyServerImpl implements IoImpl{

    private final Configuration configuration;
    private final SocketAddress address;
    private ChannelFuture closeChannelFuture;
    private NettySession nettySession;
    public NettyServerImpl(Configuration configuration) {
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


    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public NettySession bind() {
        try {
            return doBind();
        } catch (Exception e) {
            throw new SessionException(e);
        }
    }

    public NettySession doBind() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {  // 绑定客户端连接时候触发操作
                    @Override
                    protected void initChannel(SocketChannel sh) throws Exception {
                        sh.pipeline()
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new ServerHandler(configuration));
                    }
                });
        ChannelFuture future = sb.bind(address);
        Channel channel = future.channel();
        NettySessionImpl nettySession = new NettySessionImpl(channel, this, configuration);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("server start successful");
                    }
                    StartComplete startComplete = configuration.getStartComplete();
                    if (startComplete != null){
                        startComplete.handlerStartComplete(nettySession);
                    }
                } else {
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("server start fail");
                    }
                    CentralizedExceptionHandling.handlerException(future.cause());
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }
        });

        closeChannelFuture = future.channel().closeFuture();

        //listener close event
        closeChannelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (configuration.isPrintLog() && log.isInfoEnabled()) {
                        log.info("close server");
                    }
                    CloseSettlement closeSettlement = configuration.getCloseSettlement();
                    if (closeSettlement != null){
                        closeSettlement.handlerCloseSettlement(nettySession);
                    }
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }
        });
        return nettySession;
    }

    @Override
    public NettySession connect() {
        throw new WrongOperationException("this is server");
    }

    @AllArgsConstructor
    public static class ServerHandler extends ChannelInboundHandlerAdapter {

        private final Configuration configuration;

        //接受client发送的消息
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ReadMessage readMessage = configuration.getReadMessage();
            if (readMessage != null){
                readMessage.handlerReadMessage(new NettyContextBoard(ctx), msg.toString());
            }
        }

        //通知处理器最后的channelRead()是当前批处理中的最后一条消息时调用
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ReadComplete readComplete = configuration.getReadComplete();
            if (readComplete != null){
                readComplete.handlerReadComplete(new NettyContextBoard(ctx));
            }
        }

        //读操作时捕获到异常时调用
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ThrowableCaught throwableCaught = configuration.getThrowableCaught();
            if (throwableCaught != null){
                throwableCaught.handlerThrowableCaught(new NettyContextBoard(ctx), cause);
            }else {
                ctx.close();
            }
        }

        //客户端去和服务端连接成功时触发
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ConnectComplete connectComplete = configuration.getConnectComplete();
            if (connectComplete != null){
                connectComplete.handlerConnectComplete(new NettyContextBoard(ctx));
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
