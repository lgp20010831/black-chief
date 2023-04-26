package com.black.core.data;

import com.black.core.io.IoSerializer;
import com.black.core.io.ObjectSerializer;
import com.black.core.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;

public class DataInputStream extends InputStream {

    InputStream in;
    byte[] buffer;
    int count;
    protected final ObjectSerializer serializer;

    public DataInputStream(InputStream in){
        this.in = in;
        buffer = new byte[16];
        serializer = new IoSerializer();
    }

    @Override
    public int read() throws IOException {
        if (in != null){
            return in.read();
        }
        return ByteUtils.read(buffer[count++]);
    }

    public Data<?> readData(byte[] buffer, int offSet, int len) throws IOException {
        Object object = serializer.readObject(buffer, offSet, len);
        if (object instanceof Data){
            return (Data<?>) object;
        }
        return null;
    }
}
