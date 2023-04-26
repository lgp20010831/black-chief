package com.black.mq.server;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.code.ChannelHandler;
import com.black.nio.code.ChannelHandlerContext;
import com.black.nio.code.Configuration;
import com.black.nio.code.NioServerContext;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.utils.IoUtils;

import java.io.IOException;

public class NioChiefMQServer extends MQServer{

    private NioServerContext serverContext;

    public NioChiefMQServer(){

    }

    @Override
    void bind0(String host, int port) throws IOException {
        Configuration configuration = new Configuration(port, host);
        configuration.setChannelInitialization(pipeline -> {
            pipeline.addLast(new NioChannelHandler(this));
        });
        serverContext = new NioServerContext(configuration);
        serverContext.start();
    }

    @Override
    void doCloseServer() throws IOException {
        if (serverContext != null){
            serverContext.shutdownNow();
        }
    }

    protected static class NioChannelHandler implements ChannelHandler{

        private final NioChiefMQServer server;

        public NioChannelHandler(NioChiefMQServer server) {
            this.server = server;
        }

        @Override
        public void error(ChannelHandlerContext chc, Throwable e) throws IOException {
            CentralizedExceptionHandling.handlerException(e);
            chc.close();
        }

        @Override
        public void acceptComplete(ChannelHandlerContext chc) {
            String nameAddress = chc.nameAddress();
            JHexByteArrayOutputStream stream = chc.getOutputStream();
            MQOutputStream mqOutputStream = new MQOutputStream(stream, nameAddress);
            server.registerClient(mqOutputStream);
            ChannelHandler.super.acceptComplete(chc);
        }

        @Override
        public void close(ChannelHandlerContext chc) {
            String nameAddress = chc.nameAddress();
            closeClient(nameAddress);
            ChannelHandler.super.close(chc);
        }

        public void closeClient(String nameAddress){
            server.removeClient(nameAddress);
            server.removeAcitveClient(nameAddress);
        }

        @Override
        public void read(ChannelHandlerContext chc, Object source) throws IOException {
            InputServer inputServer = server.getHandlerRegister();
            byte[] bytes = IoUtils.getBytes(source, false);
            MQInputStream mqin = new MQInputStream(bytes, chc.nameAddress());
            inputServer.handleBytes(mqin);
        }
    }
}
