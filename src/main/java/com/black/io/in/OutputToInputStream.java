package com.black.io.in;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OutputToInputStream extends OutputStream {

    private byte[] buffer;

    private int pos = 0;

    public OutputToInputStream(){
        this(256);
    }

    public OutputToInputStream(int cap){
        buffer = new byte[cap];
    }

    private void doCapacity(){
        byte[] newBuffer = new byte[buffer.length + 1024];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        buffer = newBuffer;
    }

    private void check(){
        if (pos >= buffer.length){
            doCapacity();
        }
    }

    @Override
    public void write(int b) throws IOException {
        check();
        buffer[pos ++] = (byte) b;
    }

    public byte[] getBuffer(){
        byte[] target = new byte[pos];
        System.arraycopy(buffer, 0, target, 0, pos);
        return target;
    }

    public InputStream getInputStream(){
        return new ByteArrayInputStream(getBuffer());
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
