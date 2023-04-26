package com.black.nio.group.chief;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.code.ChannelHandlerContext;

import com.black.nio.group.AbstractGioContext;
import com.black.nio.group.Configuration;
import com.black.nio.group.ContextType;
import com.black.nio.group.NioType;

public class ChiefGioAdaptationContext extends AbstractGioContext {

    private final ChannelHandlerContext channelHandlerContext;

    private final Configuration configuration;

    public ChiefGioAdaptationContext(ChannelHandlerContext channelHandlerContext, Configuration configuration) {
        super(NioType.CHIEF, configuration, ContextType.SERVER);
        this.channelHandlerContext = channelHandlerContext;
        this.configuration = configuration;
    }

    @Override
    public void executeWork(Runnable runnable) {
        com.black.nio.code.Configuration configuration = channelHandlerContext.getConfiguration();
        if (configuration.isOpenWorkPool()) {
            configuration.getWorkPool().execute(runnable);
        }else {
            throw new IllegalStateException("not open work pool");
        }
    }

    @Override
    public Object source() {
        return channelHandlerContext;
    }

    @Override
    public void write(Object source) {
        channelHandlerContext.write(source);
    }

    @Override
    public void flush() {
        channelHandlerContext.flush();
    }

    @Override
    public void shutdown() {
        castIosTask(channelHandlerContext::close);
    }

    @Override
    public JHexByteArrayOutputStream getOutputStream() {
        return channelHandlerContext.getOutputStream();
    }

    @Override
    public String bindAddress() {
        com.black.nio.code.Configuration configuration = channelHandlerContext.getConfiguration();
        return configuration.getHost() + "|" + configuration.getPort();
    }

    @Override
    public String remoteAddress() {
        return channelHandlerContext.nameAddress();
    }
}
