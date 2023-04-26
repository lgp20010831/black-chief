package com.black.rpc.handler.response;

import com.black.rpc.response.Response;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;

public class BasicResponseHandler extends AbstractResponseHandler{


    @Override
    public boolean support(MethodWrapper mw, Response response) {
        Class<?> returnType = mw.getReturnType();
        String name = returnType.getName();
        return ClassWrapper.isBasic(name) || ClassWrapper.isBasicWrapper(name);
    }

    @Override
    protected Object resolverNullUtfBody(MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        String name = returnType.getName();
        if (ClassWrapper.isBasic(name)){
            switch (name){
                case "int":
                    return 0;
                case "double":
                    return 0.0;
                case "float":
                    return 0.0;
                case "long":
                    return 0;
                case "byte":
                    return 0;
                case "short":
                    return 0;
                case "boolean":
                    return false;
                case "char":
                    return ' ';
                default:
                    throw new IllegalArgumentException("is not a basic type");
            }
        }else {
            return null;
        }
    }

    @Override
    protected Object resolveUtfResponseBody(MethodWrapper mw, String utfBody) {
        Class<?> returnType = mw.getReturnType();
        String name = returnType.getName();
        String unpack = ClassWrapper.getUnpack(returnType.getSimpleName());
        switch (unpack){
            case "int":
                return Integer.parseInt(utfBody);
            case "double":
                return Double.parseDouble(utfBody);
            case "float":
                return Float.parseFloat(utfBody);
            case "long":
                return Long.parseLong(utfBody);
            case "byte":
                return Byte.parseByte(utfBody);
            case "short":
                return Short.parseShort(utfBody);
            case "boolean":
                return Boolean.parseBoolean(utfBody);
            case "char":
                return ' ';
            default:
                throw new IllegalArgumentException("is not a basic type");
        }
    }
}
