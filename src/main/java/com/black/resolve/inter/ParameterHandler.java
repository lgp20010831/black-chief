package com.black.resolve.inter;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.aop.servlet.ParameterWrapper;

public interface ParameterHandler {

    boolean support(ParameterWrapper pw);

    Object doHandler(ParameterWrapper pw, JHexByteArrayInputStream inputStream) throws Throwable;
}
