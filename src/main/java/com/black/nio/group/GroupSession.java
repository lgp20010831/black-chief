package com.black.nio.group;

import com.black.nio.code.NioContext;
import com.black.nio.netty.NettyContext;
import com.black.socket.JHexSocket;
import com.black.throwable.IOSException;
import lombok.NonNull;

import java.io.IOException;

public class GroupSession implements Session{

    private final Configuration configuration;

    private final Object source;

    public GroupSession(Configuration configuration, @NonNull Object source) {
        this.configuration = configuration;
        this.source = source;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void shutdown() {
        if (source instanceof NioContext){
            ((NioContext) source).shutdownNow();
        }

        if (source instanceof NettyContext){
            ((NettyContext) source).close();
        }

        if(source instanceof JHexSocket){
            try {
                ((JHexSocket) source).close();
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }
    }

    @Override
    public void writeAndFlushAsSocket(Object source) {
        if (this.source instanceof JHexSocket){
            try {
                ((JHexSocket) this.source).writeAndFlush(source);
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }
    }

    @Override
    public Object source() {
        return source;
    }
}
