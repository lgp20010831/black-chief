package com.black.mq.server;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.netty.*;
import com.black.core.util.CentralizedExceptionHandling;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class NettyMQServer extends MQServer{

    private NettyServerContext serverContext;

    public NettyMQServer(){
    }

    @Override
    void bind0(String host, int port) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setPort(port);
        configuration.setHost(host);
        configuration.setDispatchCallback(channelResolvers -> {
            channelResolvers.addLast(new NettyChannelHandler(this));
        });
        serverContext = new NettyServerContext(configuration);
        serverContext.bind();
    }

    @Override
    void doCloseServer() throws IOException {
        if (serverContext != null){
            serverContext.close();
        }
    }

    protected static class NettyChannelHandler implements ChannelResolver {

        private final NettyMQServer server;

        public NettyChannelHandler(NettyMQServer server) {
            this.server = server;
        }

        @Override
        public void acceptCompleted(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) {
            String name = NettyNioUtils.getNameAddress(ctx);
            MQOutputStream mqOutputStream = new MQOutputStream(out, name);
            server.registerClient(mqOutputStream);
            ChannelResolver.super.acceptCompleted(ctx, out, context);
        }

        @Override
        public void error(Throwable e, ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) throws Exception {
            CentralizedExceptionHandling.handlerException(e);
            ctx.close();
        }

        @Override
        public void close(ChannelHandlerContext ctx, NettyContext context) throws Exception {
            String name = NettyNioUtils.getNameAddress(ctx);
            closeClient(name);
            ChannelResolver.super.close(ctx, context);
        }

        public void closeClient(String nameAddress){
            server.removeClient(nameAddress);
            server.removeAcitveClient(nameAddress);
        }

        @Override
        public void read(ChannelHandlerContext ctx, byte[] bytes, JHexByteArrayOutputStream out, NettyContext context) throws Exception {
            InputServer inputServer = server.getHandlerRegister();
            String name = NettyNioUtils.getNameAddress(ctx);
            MQInputStream mqin = new MQInputStream(bytes, name);
            inputServer.handleBytes(mqin);
        }
    }
}
