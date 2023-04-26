package com.black.core.io;

import java.io.*;

public class JdkWriter implements ObjectWriter{

    public JdkWriter(){
    }

    @Override
    public byte[] writeObject(Object object) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()){
            ObjectOutputStream outputStream = new ObjectOutputStream(stream);
            outputStream.writeObject(object);
            outputStream.flush();
            outputStream.close();
            return stream.toByteArray();
        }
    }

}
