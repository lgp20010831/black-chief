package com.black.resolve.param;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.aop.servlet.ParameterWrapper;

public class InputStreamHandler extends AbstractParamHandler{
    @Override
    public boolean support(ParameterWrapper pw) {
        return pw.getType().isAssignableFrom(JHexByteArrayInputStream.class);
    }

    @Override
    public Object doHandler(ParameterWrapper pw, JHexByteArrayInputStream inputStream) throws Throwable {
        return inputStream;
    }
}
