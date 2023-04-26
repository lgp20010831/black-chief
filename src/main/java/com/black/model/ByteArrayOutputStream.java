package com.black.model;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayOutputStream extends OutputStream {

    private byte[] buf;

    private int i;

    private OutputStream out;

    public ByteArrayOutputStream(){
        this(16, null);
    }

    public ByteArrayOutputStream(OutputStream out){
        this(16, out);
    }

    public ByteArrayOutputStream(int cap, OutputStream out) {
        buf = new byte[cap];
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        if (i == -1) throw new IOException("outputstream is closed");
        if (i >= buf.length) expansion();
        buf[i++] = (byte) b;
    }

    void expansion(){
        int length = buf.length;
        byte[] newBuf = new byte[length * 2 + 8];
        System.arraycopy(buf, 0, newBuf, 0, length);
        buf = newBuf;
    }

    @Override
    public void flush() throws IOException {
        if (out != null){
            for (byte b : buf) {
                out.write(b);
            }
            out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        i = -1;
        if (out != null) out.close();
    }

    public byte[] toByteArray(){
        return buf;
    }
}
