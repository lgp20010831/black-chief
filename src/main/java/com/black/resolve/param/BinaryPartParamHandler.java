package com.black.resolve.param;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.BinaryPartElement;
import com.black.resolve.annotation.BinaryPart;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.utils.ServiceUtils;

import java.io.IOException;
import java.util.Collection;

public class BinaryPartParamHandler extends AbstractParamHandler{

    @Override
    public boolean support(ParameterWrapper pw) {
        return pw.hasAnnotation(BinaryPart.class);
    }

    @Override
    public Object doHandler(ParameterWrapper pw, JHexByteArrayInputStream inputStream) throws Throwable {
        Class<?> type = pw.getType();
        Object result;
        if (isCollection(pw)) {
            Class<?> generic = getGenericByCollection(pw);
            Collection<Object> collection = ServiceUtils.createCollection(type);
            for (;;){
                BinaryPartElement partElement;
                try {
                    partElement = inputStream.readBinary();
                }catch (IOException e){
                    break;
                }
                Object part = TypeConvertCache.initAndGet().convert(generic, partElement);
                collection.add(part);
            }
            result = collection;
        }else {
            BinaryPartElement partElement = inputStream.readBinary();
            result = TypeConvertCache.initAndGet().convert(type, partElement);
        }
        return result;
    }
}
