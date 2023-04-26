package com.black.netty;

import com.black.netty.ill.SessionException;

public class NettyServerSessionFactory implements SessionFactory<NettySession>{
    private final Configuration configuration;

    public NettyServerSessionFactory(Configuration configuration) {
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

        NettyServerImpl serverImpl = new NettyServerImpl(configuration);
        try {
             return serverImpl.bind();
        } catch (Exception e) {
            throw new SessionException(e);
        }
    }
}
