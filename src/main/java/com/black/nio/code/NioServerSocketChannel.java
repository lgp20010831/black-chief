package com.black.nio.code;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class NioServerSocketChannel extends AbstractNioChannel{

    public NioServerSocketChannel(SelectableChannel channel, Configuration configuration, String nameAddress) {
        super(channel, configuration, nameAddress);
    }

    public NioServerSocketChannel(ServerSocketChannel serverSocketChannel,
                                  Configuration configuration, SelectionKey key,
                                  EventLoop loop, String name) {
        super(serverSocketChannel, configuration, key, loop, name);
    }



}
