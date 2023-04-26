package com.black.core.io;

import java.io.IOException;

public final class IoSerializer implements ObjectSerializer {

    private ObjectWriter writer;
    private ObjectReader reader;
    public IoSerializer(){

        try {
            writer = new HessianWriter();
        } catch (ClassNotFoundException e) {
            writer = new JdkWriter();
        }

        try {
            reader = new HessianReader();
        } catch (ClassNotFoundException e) {
            reader = new JdkReader();
        }
    }

    @Override
    public Object readObject(byte[] buffer) throws IOException {
        return reader.readObject(buffer);
    }

    @Override
    public Object readObject(byte[] buffer, int offset, int length) throws IOException {
        return reader.readObject(buffer, offset, length);
    }

    @Override
    public <T> T copyObject(T obj) {
        try {
            return (T) readObject(writeObject(obj));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] writeObject(Object object) throws IOException {
        return writer.writeObject(object);
    }
}
