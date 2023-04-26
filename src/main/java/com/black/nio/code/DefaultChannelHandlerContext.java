package com.black.nio.code;

public class DefaultChannelHandlerContext extends AbstractChannelHandlerContext{

    public DefaultChannelHandlerContext(ChannelHandler handler, NioChannel channel) {
        super(handler, channel);
    }
}
