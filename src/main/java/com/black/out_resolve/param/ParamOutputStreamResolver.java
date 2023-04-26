package com.black.out_resolve.param;

import com.black.io.out.JHexByteArrayOutputStream;

import java.lang.reflect.Parameter;

public interface ParamOutputStreamResolver {

    boolean support(Parameter parameter);

    void resolve(JHexByteArrayOutputStream outputStream, Parameter parameter, Object value) throws Throwable;
}
