package com.black.nio.code;

import lombok.NonNull;

import java.io.IOException;

public class ServerEventLoopGroup extends DefaultEventLoopGroup{

    private final AbstractNioServerContext nioServerContext;

    public ServerEventLoopGroup(@NonNull Configuration configuration) throws IOException {
        super(configuration);
        this.nioServerContext = (AbstractNioServerContext) configuration.getContext();
    }

    @Override
    protected void loopCreate(int size) throws IOException {
        super.loopCreate(1);
    }

    @Override
    protected EventLoop createEventLoop() throws IOException {
        return new ServerEventLoop(configuration, this, (AbstractNioServerContext) configuration.getContext());
    }
}
