package com.black.nio.netty;

import com.black.io.out.JHexByteArrayOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public interface ChannelResolver {

    void read(ChannelHandlerContext ctx, byte[] bytes, JHexByteArrayOutputStream out, NettyContext context) throws Exception;

    default void acceptCompleted(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context){

    }

    default void connectCompleted(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context){

    }

    default void write(ChannelHandlerContext ctx, byte[] bytes, JHexByteArrayOutputStream out, ChannelPromise promise, NettyContext context)throws Exception{

    }

    default void flush(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context)throws Exception{

    }

    default void active(ChannelHandlerContext ctx, NettyContext context)throws Exception{

    }

    default void error(Throwable e, ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context)throws Exception{

    }

    default void close(ChannelHandlerContext ctx, NettyContext context)throws Exception{

    }


}
