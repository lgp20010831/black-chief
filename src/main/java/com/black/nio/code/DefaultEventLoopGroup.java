package com.black.nio.code;

import lombok.NonNull;

import java.io.IOException;

public class DefaultEventLoopGroup extends AbstractEventLoopGroup{

    public DefaultEventLoopGroup(@NonNull Configuration configuration) throws IOException {
        super(configuration);
    }

    @Override
    protected EventLoop createEventLoop() throws IOException {
        return new DefaultEventLoop(configuration, this);
    }
}
