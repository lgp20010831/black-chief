package com.black.resolve.param;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.resolve.annotation.UtfBody;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.utils.ServiceUtils;

import java.io.IOException;
import java.util.Collection;

public class UtfBodyHandler extends AbstractParamHandler {

    @Override
    public boolean support(ParameterWrapper pw) {
        return pw.hasAnnotation(UtfBody.class);
    }

    @Override
    public Object doHandler(ParameterWrapper pw, JHexByteArrayInputStream inputStream) throws Throwable {
        Class<?> type = pw.getType();
        Object result;
        if (isCollection(pw)){
            Class<?> generic = getGenericByCollection(pw);
            Collection<Object> collection = ServiceUtils.createCollection(type);
            for (;;){
                Object utf;
                try {
                     utf = doReadUtf(inputStream);
                }catch (IOException e){
                    break;
                }
                Object convert = TypeConvertCache.initAndGet().convert(generic, utf);
                collection.add(convert);
            }
            result = collection;
        }else {
            Object utf = doReadUtf(inputStream);
            result = TypeConvertCache.initAndGet().convert(type, utf);
        }
        return result;
    }

    private Object doReadUtf(JHexByteArrayInputStream inputStream) throws IOException {
        return inputStream.readUTF();
    }


}
