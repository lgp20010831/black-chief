package com.black.netty;

import io.netty.channel.ChannelHandlerContext;

public class NettyContextBoard implements NettySessionBoard {

    private final ChannelHandlerContext handlerContext;

    public NettyContextBoard(ChannelHandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    @Override
    public void write(String message) {
        handlerContext.write(message);
    }

    @Override
    public void writeAndFlush(String message) {
        handlerContext.writeAndFlush(message);
    }

    @Override
    public void close() {
        handlerContext.close();
    }

    @Override
    public ChannelHandlerContext getContext() {
        return handlerContext;
    }

}
