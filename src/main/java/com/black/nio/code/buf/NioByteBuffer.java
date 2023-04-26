package com.black.nio.code.buf;

import com.black.nio.code.Configuration;
import com.black.nio.code.NioChannel;

import java.io.IOException;

public interface NioByteBuffer {

    Configuration getConfiguration();

    void writeBytes(byte[] bytes);

    void write(NioChannel channel) throws IOException;

    byte[] read(NioChannel channel) throws IOException, SocketReadCloseException;

    byte[] toByteArray();

    void release();

    void clear();

}
