package com.black.nio.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class ByteBufCaseToByteResolver implements ChannelResolverAdapter {

    public static final ByteBufCaseToByteResolver DEFAULT = new ByteBufCaseToByteResolver();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof byte[]){
            ctx.fireChannelRead(msg);
            return;
        }

        if (msg instanceof ByteBuf){
            byte[] bytes = doCastBytes((ByteBuf) msg);
            ctx.fireChannelRead(bytes);
            return;
        }

        throw new IOException("the processor should not intervene in converting bytes");
    }

    private byte[] doCastBytes(ByteBuf buf){
        return ByteBufUtil.getBytes(buf);
    }
}
