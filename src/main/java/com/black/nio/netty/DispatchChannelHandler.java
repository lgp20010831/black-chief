package com.black.nio.netty;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.utils.IoUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.LinkedList;

public class DispatchChannelHandler extends LinkedList<ChannelResolver> implements ChannelResolverAdapter{

    private final NettyContext context;

    private final Channel channel;

    private JHexByteArrayOutputStream outputStream;

    public DispatchChannelHandler(NettyContext context, Channel channel) {
        this.context = context;
        this.channel = channel;
        NettyChannelOutputStream channelOutputStream = new NettyChannelOutputStream(channel);
        outputStream = new JHexByteArrayOutputStream(channelOutputStream);
    }

    private JHexByteArrayOutputStream getOutputStream(){
        return outputStream;
    }


    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ChannelResolverAdapter.super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (ChannelResolver resolver : this) {
            resolver.active(ctx, context);
        }
        if (context instanceof NettyServerContext){
            for (ChannelResolver resolver : this) {
                resolver.acceptCompleted(ctx, getOutputStream(), context);
            }
        }
        ChannelResolverAdapter.super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        for (ChannelResolver resolver : this) {
            resolver.close(ctx, context);
        }
        ChannelResolverAdapter.super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes = IoUtils.castToBytes(msg);
        JHexByteArrayOutputStream outputStream = getOutputStream();
        for (ChannelResolver resolver : this) {
            resolver.read(ctx, bytes, outputStream, context);
        }
        //ChannelResolverAdapter.super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        JHexByteArrayOutputStream outputStream = getOutputStream();
        for (ChannelResolver resolver : this) {
            resolver.error(cause, ctx, outputStream, context);
        }
        ChannelResolverAdapter.super.exceptionCaught(ctx, cause);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ChannelResolverAdapter.super.close(ctx, promise);
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] bytes = IoUtils.castToBytes(msg);
        JHexByteArrayOutputStream outputStream = getOutputStream();
        for (ChannelResolver resolver : this) {
            resolver.write(ctx, bytes, outputStream, promise, context);
        }
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buffer.writeBytes(bytes);
        ChannelResolverAdapter.super.write(ctx, buffer, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        JHexByteArrayOutputStream outputStream = getOutputStream();
        for (ChannelResolver resolver : this) {
            resolver.flush(ctx, outputStream, context);
        }
        ChannelResolverAdapter.super.flush(ctx);
    }
}
