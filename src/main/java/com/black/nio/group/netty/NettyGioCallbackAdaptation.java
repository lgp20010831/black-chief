package com.black.nio.group.netty;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.Configuration;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.nio.netty.ChannelResolver;
import com.black.nio.netty.NettyContext;
import com.black.core.util.Assert;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyGioCallbackAdaptation implements ChannelResolver {

    private final Configuration configuration;

    private Map<Channel, GioContext> contextCache = new ConcurrentHashMap<>();

    public NettyGioCallbackAdaptation(Configuration configuration) {
        this.configuration = configuration;
    }

    private GioContext getContext(ChannelHandlerContext chc){
        GioContext gioContext = contextCache.get(chc.channel());
        Assert.notNull(gioContext, "not register context in channel:" + chc.channel());
        return gioContext;
    }

    @Override
    public void read(ChannelHandlerContext ctx, byte[] bytes, JHexByteArrayOutputStream out, NettyContext context) throws Exception {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(ctx);
        resolver.read(gioContext, bytes, out);
    }

    @Override
    public void acceptCompleted(ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = contextCache.computeIfAbsent(ctx.channel(), channel -> {
            return new NettyGioAdaptationContext(context, ctx, out, configuration);
        });
        resolver.acceptCompleted(gioContext, gioContext.getOutputStream());
        ChannelResolver.super.acceptCompleted(ctx, out, context);
    }

    @Override
    public void write(ChannelHandlerContext ctx, byte[] bytes, JHexByteArrayOutputStream out, ChannelPromise promise, NettyContext context) throws Exception {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(ctx);
        resolver.write(gioContext, bytes);
        ChannelResolver.super.write(ctx, bytes, out, promise, context);
    }

    @Override
    public void error(Throwable e, ChannelHandlerContext ctx, JHexByteArrayOutputStream out, NettyContext context) throws Exception {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(ctx);
        resolver.trowable(gioContext, e, out);
        ChannelResolver.super.error(e, ctx, out, context);
    }

    @Override
    public void close(ChannelHandlerContext ctx, NettyContext context) throws Exception {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(ctx);
        resolver.close(gioContext);
        ChannelResolver.super.close(ctx, context);
    }
}
