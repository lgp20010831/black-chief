package com.black.nio.code;

import com.black.nio.code.run.Future;

import java.nio.channels.ClosedChannelException;

public interface EventLoopGroup {

    Configuration getConfiguration();

    void close();

    EventLoop get();

    Future<NioChannel> registerChannel(NioChannel channel, int interOps) throws ClosedChannelException;
}
