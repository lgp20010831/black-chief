package com.black.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class NettySessionImpl implements NettySession{
    private final Channel channel;
    private final IoImpl impl;
    private final Configuration configuration;

    public NettySessionImpl(Channel channel, IoImpl impl, Configuration configuration) {
        this.channel = channel;
        this.impl = impl;
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void write(String message) {
        ChannelFuture future = channel.write(message);
    }

    @Override
    public void writeAndFlush(String message) {
        channel.writeAndFlush(message);
    }

    @Override
    public void close() {
        ChannelFuture future = channel.close();
    }

    @Override
    public void restart() {
        if (impl.isServer()) {
            impl.bind();
        }else {
            impl.connect();
        }
    }
}
