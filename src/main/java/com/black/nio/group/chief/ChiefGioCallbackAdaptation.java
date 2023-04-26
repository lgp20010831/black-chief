package com.black.nio.group.chief;

import com.black.nio.code.ChannelHandler;
import com.black.nio.code.ChannelHandlerContext;
import com.black.nio.code.NioChannel;
import com.black.nio.group.Configuration;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.core.log.IoLog;
import com.black.core.util.Assert;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChiefGioCallbackAdaptation implements ChannelHandler {

    private final Configuration configuration;

    private final Map<NioChannel, GioContext> contextCache = new ConcurrentHashMap<>();

    public ChiefGioCallbackAdaptation(Configuration configuration) {
        this.configuration = configuration;
    }

    private GioContext getContext(ChannelHandlerContext chc){
        GioContext gioContext = contextCache.computeIfAbsent(chc.channel(), channel -> {
            return new ChiefGioAdaptationContext(chc, configuration);
        });
        Assert.notNull(gioContext, "not register context in channel:" + chc.channel());
        return gioContext;
    }

    @Override
    public void acceptComplete(ChannelHandlerContext chc) {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = contextCache.computeIfAbsent(chc.channel(), channel -> {
            return new ChiefGioAdaptationContext(chc, configuration);
        });
        IoLog log = configuration.getLog();
        log.info("[GIO] [CHIEF] accept connect as channel: {}", chc.channel());
        resolver.acceptCompleted(gioContext, gioContext.getOutputStream());
        ChannelHandler.super.acceptComplete(chc);
    }

    @Override
    public void error(ChannelHandlerContext chc, Throwable e) throws IOException {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext context = getContext(chc);
        resolver.trowable(context, e, context.getOutputStream());
        ChannelHandler.super.error(chc, e);
    }

    @Override
    public void write(ChannelHandlerContext chc, Object source) throws IOException {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext context = getContext(chc);
        resolver.write(context, source);
        ChannelHandler.super.write(chc, source);
    }

    @Override
    public void close(ChannelHandlerContext chc) {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext context = getContext(chc);
        resolver.close(context);
        ChannelHandler.super.close(chc);
    }

    @Override
    public void read(ChannelHandlerContext chc, Object source) throws IOException {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext context = getContext(chc);
        resolver.read(context, IoUtils.getBytes(source), context.getOutputStream());
    }
}
