package com.black.nio.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.SocketAddress;

public class NettyNioUtils {


    public static String getNameAddress(ChannelHandlerContext ctx){
        return getNameAddress(ctx.channel());
    }

    public static String getNameAddress(Channel channel){
        SocketAddress address = channel.remoteAddress();
        return address == null ? "no remote address" : address.toString();
    }
}
