package com.black.rpc;

import com.alibaba.fastjson.JSON;
import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.rpc.annotation.InputBody;
import com.black.rpc.inter.RpcMessageConvertHandler;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.query.MethodWrapper;

import java.io.IOException;

public class DefaultMessageConvertHandler implements RpcMessageConvertHandler {

    @Override
    public Object convertRequest(DataByteBufferArrayInputStream inputStream, MethodWrapper mw) throws IOException {
        String utf = inputStream.readUTF();
        System.out.println("param body:" + utf);
        if (mw.parameterHasAnnotation(InputBody.class)) {
            ParameterWrapper parameter = mw.getSingleParameterByAnnotation(InputBody.class);
            Class<?> type = parameter.getType();
            TypeHandler handler = TypeConvertCache.initAndGet();
            return handler.convert(type, utf);
        }
        return JSON.parse(utf);
    }

    @Override
    public Object convertResponse(DataByteBufferArrayInputStream inputStream, MethodWrapper mw) throws IOException {
        String utf = inputStream.readUTF();
        Class<?> returnType = mw.getReturnType();
        if (returnType.equals(String.class)){
            return utf;
        }
        return utf;
    }
}
