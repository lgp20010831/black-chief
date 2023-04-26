package com.black.resolve.param;

import com.alibaba.fastjson.JSON;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.resolve.ResolveException;
import com.black.resolve.annotation.JsonBody;
import com.black.resolve.inter.JhexStringStreamReader;
import com.black.resolve.inter.StringStreamReader;
import com.black.core.aop.servlet.ParameterWrapper;
import lombok.NonNull;

public class JsonBeanParamHandler extends AbstractParamHandler{

    public static volatile StringStreamReader stringStreamReader;

    static {
        stringStreamReader = new JhexStringStreamReader();
    }

    public static void setStringStreamReader(@NonNull StringStreamReader stringStreamReader) {
        JsonBeanParamHandler.stringStreamReader = stringStreamReader;
    }

    @Override
    public boolean support(ParameterWrapper pw) {
        return pw.hasAnnotation(JsonBody.class);
    }

    @Override
    public Object doHandler(ParameterWrapper pw, JHexByteArrayInputStream inputStream) throws Throwable {
        Class<?> type = pw.getType();
        String body;
        synchronized (this){
            body = stringStreamReader.readString(inputStream);
        }
        Object json = JSON.parse(body);
        if (json instanceof JSON){
            return JSON.toJavaObject((JSON) json, type);
        }else {
            throw new ResolveException("parameter cannot be resolved to json:" + body);
        }
    }
}
