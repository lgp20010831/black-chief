package com.black.netty;

import com.black.netty.ill.SessionException;


public class NettySessionFactory implements SessionFactory<NettySession> {

    private final Configuration configuration;

    public NettySessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public NettySession openSession() {
        if (configuration == null){
            throw new SessionException("configuration must not be null");
        }
        NettyClientImpl clientImpl = new NettyClientImpl(configuration);
        try {
            return clientImpl.start();
        } catch (Exception e) {
            throw new SessionException(e);
        }
    }
}
