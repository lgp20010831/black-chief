package com.black.nio.code;

import lombok.extern.log4j.Log4j2;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Log4j2
public class NioSocketChannel extends AbstractNioChannel{

    public NioSocketChannel(SelectableChannel channel, Configuration configuration, String nameAddress) {
        super(channel, configuration, nameAddress);
    }

    public NioSocketChannel(SocketChannel channel, Configuration configuration,
                            SelectionKey key, EventLoop loop, String name) {
        super(channel, configuration, key, loop, name);
    }

    public NioChannel reconnect(){
        log.info("reconnect...");
        NioClientContext context = (NioClientContext) configuration.getContext();
        context.connect();
        return context.getNioChannel();
    }

}
