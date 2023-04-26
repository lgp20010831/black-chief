package com.black.nio.code;


import com.black.nio.code.run.Future;
import lombok.NonNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractEventLoopGroup implements EventLoopGroup{

    protected final Configuration configuration;

    private EventLoop[] loops;

    private final AtomicInteger IDX = new AtomicInteger(0);

    public AbstractEventLoopGroup(@NonNull Configuration configuration) throws IOException {
        this.configuration = configuration;
        loopCreate(configuration.getGroupSize());
    }

    protected void loopCreate(int size) throws IOException {
        if (size < 0){
            throw new IllegalArgumentException("GROUP SIZE < 0");
        }
        if (size > 1){
            size = mapfor(size);
        }
        loops = new EventLoop[size];
        for (int i = 0; i < loops.length; i++) {
            loops[i] = createEventLoop();
        }
    }

    public static int mapfor(int i){
        int n = i - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 1024) ? 1024 : n + 1;
    }

    protected abstract EventLoop createEventLoop() throws IOException;

    public Configuration getConfiguration() {
        return configuration;
    }

    public EventLoop get(){
        return loops[IDX.getAndIncrement() & loops.length - 1];
    }

    @Override
    public Future<NioChannel> registerChannel(NioChannel channel, int interOps) {
        return get().registerChannel(channel, interOps);
    }

    @Override
    public void close() {
        for (EventLoop loop : loops) {
            loop.close();
        }
    }
}
