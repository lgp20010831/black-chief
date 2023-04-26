package com.black.core.io;

import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class HessianWriter implements ObjectWriter {

    public HessianWriter() throws ClassNotFoundException {
        Class.forName("com.caucho.hessian.io.Hessian2Output");
    }

    @Override
    public byte[] writeObject(Object object) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();){
            Hessian2Output hessian2Output = new Hessian2Output(stream);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            hessian2Output.close();
            return stream.toByteArray();
        }
    }
}