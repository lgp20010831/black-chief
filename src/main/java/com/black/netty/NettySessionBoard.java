package com.black.netty;

import io.netty.channel.ChannelHandlerContext;

public interface NettySessionBoard {

    void write(String message);

    void writeAndFlush(String message);

    void close();

    ChannelHandlerContext getContext();

}
