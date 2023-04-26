package com.black.core.io;

import com.caucho.hessian.io.Hessian2Input;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class HessianReader implements ObjectReader{

    public HessianReader() throws ClassNotFoundException {
        Class.forName("com.caucho.hessian.io.Hessian2Input");
    }

    @Override
    public Object readObject(byte[] buffer) throws IOException {
        return readObject(buffer, 0, buffer.length);
    }

    @Override
    public Object readObject(byte[] buffer, int offset, int length) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer, offset, length);
        Hessian2Input hessian2Input = new Hessian2Input(stream);
        Object target = hessian2Input.readObject();
        hessian2Input.close();
        return target;
    }
}
