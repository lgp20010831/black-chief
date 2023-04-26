package com.black.model;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStream extends InputStream {

    private final byte[] buf;

    private int i;

    public ByteArrayInputStream(byte[] buf) {
        this.buf = buf;
    }

    public byte[] toByteArray(){
        return buf;
    }

    @Override
    public int available() throws IOException {
        return buf.length;
    }

    @Override
    public int read() throws IOException {
        if (i == -1) throw new IOException("inputstream is closed");
        return buf[i++];
    }

    @Override
    public synchronized void reset() throws IOException {
        i = 0;
    }

    @Override
    public void close() throws IOException {
        i = -1;
    }
}
