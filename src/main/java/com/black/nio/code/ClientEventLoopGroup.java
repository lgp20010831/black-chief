package com.black.nio.code;

import lombok.NonNull;

import java.io.IOException;

public class ClientEventLoopGroup extends DefaultEventLoopGroup{

    public ClientEventLoopGroup(@NonNull Configuration configuration) throws IOException {
        super(configuration);
    }

    @Override
    protected void loopCreate(int size) throws IOException {
        super.loopCreate(1);
    }


    @Override
    protected EventLoop createEventLoop() throws IOException {
        return new ClientEventLoop(configuration, this);
    }
}
