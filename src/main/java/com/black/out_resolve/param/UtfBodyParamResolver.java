package com.black.out_resolve.param;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.resolve.annotation.UtfBody;

import java.io.IOException;
import java.lang.reflect.Parameter;

public class UtfBodyParamResolver extends AbstractParamResolver {

    @Override
    public boolean support(Parameter parameter) {
        return parameter.isAnnotationPresent(UtfBody.class);
    }

    @Override
    public void resolve(JHexByteArrayOutputStream outputStream, Parameter parameter, Object value) throws IOException {
        String stringValue = getStringValue(value);
        outputStream.writeUTF(stringValue);
    }
}
