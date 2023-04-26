package com.black.pool.bytes;

import com.black.pool.Closeable;

import java.util.Arrays;

public class Bytes implements Closeable {

    private final byte[] buffer;

    private int pos = 0;

    public Bytes(){
        this(12);
    }

    public Bytes(int size) {
        buffer = new byte[size];
    }

    private void check(int i){
        if (pos + i > buffer.length){
            throw new IndexOutOfBoundsException();
        }
    }

    public void addBytes(byte[] bytes){
        check(bytes.length);
        System.arraycopy(bytes, 0, buffer, pos, bytes.length);
        pos += bytes.length;
    }

    public void addByte(byte b){
        check(1);
        buffer[pos ++] = b;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public void close(){
        Arrays.fill(buffer, (byte) 0);
        pos = 0;
    }
}
