package com.black.core.data;

import com.black.core.io.IoSerializer;
import com.black.core.io.ObjectSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class DataOutputStream extends OutputStream {

    OutputStream out;

    protected int count;

    protected byte[] buffer;

    protected final ObjectSerializer serializer;

    public DataOutputStream(){
        this(null);
    }

    public DataOutputStream(OutputStream out){
        this.out = out;
        serializer = new IoSerializer();
        buffer = new byte[16];
    }

    public byte[] getBuffer(){
        return buffer;
    }

    public byte[] toByteArray(){
        byte[] bytes = new byte[getCount()];
        System.arraycopy(buffer, 0, bytes, 0, getCount());
        return bytes;
    }

    @Override
    public void write(int b) throws IOException {
        adjustBuffer(1);
        buffer[count++] = (byte) b;
    }

    public int getCount(){
        return count;
    }

    public void write(Data<?> data) throws IOException{
         if (data == null){
             return;
         }
        byte[] bytes = serializer.writeObject(data);
        for (byte b : bytes) {
            write(b);
        }
    }

    protected void adjustBuffer(int len){
        int blen = buffer.length;
        if (count == blen || count + len > blen){
            byte[] newBuffer = new byte[blen * 2 + len];
            System.arraycopy(buffer, 0, newBuffer, 0, count);
            buffer = newBuffer;
        }
    }

    @Override
    public void flush() throws IOException {
        if (out != null){
            for (byte b : buffer) {
                out.write(b);
            }
            out.flush();
        }

        Arrays.fill(buffer, 0, buffer.length - 1, (byte) 0);
        count = 0;
    }

    @Override
    public void close() throws IOException {
        flush();
        if (out != null){
            out.close();
        }
    }
}
