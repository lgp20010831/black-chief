package com.black.core.io;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class JdkReader implements ObjectReader{

    public JdkReader(){
    }

    @Override
    public Object readObject(byte[] buffer) throws IOException {
        return readObject(buffer, 0, buffer.length);
    }

    @Override
    public Object readObject(byte[] buffer, int offset, int length) throws IOException {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, offset, length)){
            ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
            Object target;
            try {
                target = inputStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("can not read bean", e);
            }
            inputStream.close();
            return target;
        }
    }
}
