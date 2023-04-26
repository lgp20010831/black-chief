package com.black.resolve.param;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.aop.servlet.ParameterWrapper;

public class BytesParamHandler extends AbstractParamHandler{
    @Override
    public boolean support(ParameterWrapper pw) {
        return byte[].class.equals(pw.getType());
    }

    @Override
    public Object doHandler(ParameterWrapper pw, JHexByteArrayInputStream inputStream) throws Throwable {
        return inputStream.readAll();
    }
}
