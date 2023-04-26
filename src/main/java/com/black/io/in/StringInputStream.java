package com.black.io.in;

import com.black.core.util.ByteUtils;
import java.io.IOException;
import java.io.InputStream;

public class StringInputStream extends InputStream {

    private byte[] buf;

    private int i;

    public StringInputStream(){
        buf = new byte[0];
    }

    public StringInputStream(String source){
        buf = convertBytes(source);
    }

    byte[] convertBytes(String str){
        return str == null ? new byte[]{'n', 'u', 'l', 'l'} : str.getBytes();
    }

    @Override
    public int read() throws IOException {
        return i + 1 > buf.length ? -1 : ByteUtils.read(buf[i++]);
    }

    public void append(String str){
        byte[] bytes = convertBytes(str);
        byte[] newBuf = new byte[buf.length + bytes.length];
        System.arraycopy(buf, 0, newBuf, 0, buf.length);
        buf = newBuf;
    }

    public byte[] toByteArray(){
        byte[] newBuf = new byte[buf.length];
        System.arraycopy(buf, 0, newBuf, 0, buf.length);
        return newBuf;
    }

    @Override
    public String toString() {
        return new String(buf);
    }

    @Override
    public void close() throws IOException {
        buf = new byte[0];
    }
}
