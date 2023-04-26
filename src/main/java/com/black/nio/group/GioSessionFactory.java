package com.black.nio.group;

import com.black.nio.code.NioServerContext;
import com.black.nio.group.bio.JHexServerGioCallbackAdaptation;
import com.black.nio.group.chief.ChiefGioCallbackAdaptation;
import com.black.nio.group.jhex.JHexGioCallbackAdaptation;
import com.black.nio.group.netty.NettyGioCallbackAdaptation;
import com.black.nio.netty.NettyServerContext;
import com.black.socket.JHexSocket;
import com.black.socket.server.JHexServerSocket;
import com.black.socket.server.SingleServerSocket;
import com.black.core.log.IoLog;
import com.black.throwable.IOSException;

import java.io.IOException;


public class GioSessionFactory implements SessionFactory{

    private final Configuration configuration;

    public GioSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Session openSession(){
        try {
            NioType nioType = configuration.getNioType();
            IoLog log = configuration.getLog();
            log.info("[GIO] type: [{}]", nioType);
            switch (nioType){
                case JHEX:
                    return createJhexAndOpen();
                case CHIEF:
                    return createChiefAndOpen();
                case NETTY:
                    return createNettyAndOpen();
                case BIO:
                    return createJHexServerAndOpen();
                default:
                    throw new IllegalStateException("ill nio type:" + nioType);
            }

        }catch (Throwable e){
            throw new IOSException("OPEN FAIR", e);
        }
    }

    private Session createJHexServerAndOpen(){
        IoLog log = this.configuration.getLog();
        JHexServerSocket serverSocket = new SingleServerSocket(configuration.getHost(), configuration.getPort());
        serverSocket.setSocketHandler(new JHexServerGioCallbackAdaptation(configuration, serverSocket));
        serverSocket.setLog(configuration.getLog());
        serverSocket.bind();
        log.info("[GIO] BIO BIND FINISH");
        return new GroupSession(this.configuration, serverSocket);
    }

    private Session createJhexAndOpen() throws IOException {
        IoLog log = this.configuration.getLog();
        JHexSocket jHexSocket = new JHexSocket(configuration.getHost(), configuration.getPort());
        JHexGioCallbackAdaptation callbackAdaptation = new JHexGioCallbackAdaptation(configuration, jHexSocket);
        jHexSocket.setJHexBytesHandler(callbackAdaptation);
        jHexSocket.setLog(configuration.getLog());
        jHexSocket.connect();
        log.info("[GIO] JHEX CONNECT FINISH");
        callbackAdaptation.connectFinish();
        return new GroupSession(configuration, jHexSocket);
    }

    private Session createChiefAndOpen() throws IOException {
        IoLog log = this.configuration.getLog();
        com.black.nio.code.Configuration configuration = new com.black.nio.code.Configuration();
        configuration.setPort(this.configuration.getPort());
        configuration.setHost(this.configuration.getHost());
        configuration.setGroupSize(this.configuration.getIoThreadNum());
        configuration.setOpenWorkPool(true);
        configuration.setChannelInitialization(pipeline -> {
            pipeline.addLast(new ChiefGioCallbackAdaptation(this.configuration));
        });
        NioServerContext serverContext = new NioServerContext(configuration);
        serverContext.start();
        log.info("[GIO] CHIEF BIND FINISH");
        return new GroupSession(this.configuration, serverContext);
    }

    private Session createNettyAndOpen(){
        IoLog log = this.configuration.getLog();
        com.black.nio.netty.Configuration configuration = new com.black.nio.netty.Configuration();
        configuration.setPort(this.configuration.getPort());
        configuration.setHost(this.configuration.getHost());
        configuration.setOpenWorkPool(true);
        configuration.setIoEventThreadNum(this.configuration.getIoThreadNum());
        configuration.setDispatchCallback(channelResolvers -> {
            channelResolvers.addLast(new NettyGioCallbackAdaptation(this.configuration));
        });
        NettyServerContext serverContext = new NettyServerContext(configuration);
        serverContext.bind();
        log.info("[GIO] NETTY BIND FINISH");
        return new GroupSession(this.configuration, serverContext);
    }
}
