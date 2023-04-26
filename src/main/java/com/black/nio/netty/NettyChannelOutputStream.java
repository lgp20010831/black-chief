package com.black.nio.netty;

import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NettyChannelOutputStream extends OutputStream {

    private final Channel channel;

    private ByteArrayOutputStream out;

    public NettyChannelOutputStream(Channel channel) {
        this.channel = channel;
        out = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void flush() throws IOException {
        byte[] bytes = out.toByteArray();
        out.reset();
        channel.writeAndFlush(bytes);
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }
}
