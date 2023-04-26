package com.black.nio.code;

import lombok.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NioChannelOutputStream extends OutputStream {

    private final NioChannel channel;

    private ByteArrayOutputStream out;

    public NioChannelOutputStream(@NonNull NioChannel channel) {
        this.channel = channel;
        Configuration configuration = channel.getConfiguration();
        out = new ByteArrayOutputStream(configuration.getAcceptBufferSize());
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void flush() throws IOException {
        byte[] byteArray = out.toByteArray();
        channel.writeAndFlush(byteArray);
        out.reset();
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }
}
