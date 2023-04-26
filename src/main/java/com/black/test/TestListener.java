package com.black.test;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;

import java.util.Map;

public interface TestListener {


    default String postUrl(String url, MethodWrapper mw, ClassWrapper<?> cw){
        return url;
    }

    default void postHttpHeaders(Map<String, String> headers, MethodWrapper mw, ClassWrapper<?> cw){

    }

    default Object postRequestParam(Object param, MethodWrapper mw, ClassWrapper<?> cw){
        return param;
    }

    default String postResponse(String response, MethodWrapper mw, ClassWrapper<?> cw){
        return response;
    }
}
