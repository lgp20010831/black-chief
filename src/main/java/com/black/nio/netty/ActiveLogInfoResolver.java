package com.black.nio.netty;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.code.AttysNioException;
import com.black.core.log.IoLog;
import com.black.core.util.CentralizedExceptionHandling;
import io.netty.channel.ChannelHandlerContext;

import java.net.SocketAddress;

public abstract class ActiveLogInfoResolver implements ChannelResolver{


    @Override
    public void acceptCompleted(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) {
        IoLog log = context.getLog();
        SocketAddress address = ctx.channel().remoteAddress();
        log.debug("接到客户端连接: {}", address);
    }

    @Override
    public void connectCompleted(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) {
        IoLog log = context.getLog();
        SocketAddress address = ctx.channel().remoteAddress();
        log.debug("成功连接上服务器: {}", address);
    }

    @Override
    public void close(ChannelHandlerContext ctx, NettyContext context) throws Exception {
        IoLog log = context.getLog();
        SocketAddress address = ctx.channel().remoteAddress();
        log.debug("客户端断开连接: {}", address);
    }

    @Override
    public void active(ChannelHandlerContext ctx, NettyContext context) throws Exception {

    }

    @Override
    public void error(Throwable e, ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) throws Exception {
        if (e instanceof AttysNioException){
            CentralizedExceptionHandling.handlerException(e);
        }
        SocketAddress address = ctx.channel().remoteAddress();
        IoLog log = context.getLog();
        log.error(null, "客户端: {} 发生异常:{}", address, e.getMessage());
    }
}
