package com.black.rpc.handler.json;

import com.alibaba.fastjson.JSONObject;
import com.black.rpc.annotation.Input;
import com.black.rpc.ill.MethodInvokerParamWiredException;
import com.black.rpc.inter.JsonRequestHandler;
import com.black.rpc.request.Request;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;

public class InputParamResolver implements JsonRequestHandler {
    @Override
    public boolean support(MethodWrapper mw, ParameterWrapper pw) {
        return pw.hasAnnotation(Input.class);
    }

    @Override
    public Object wiredParm(MethodWrapper mw, ParameterWrapper pw, JSONObject paramJson, Request request) {
        Input annotation = pw.getAnnotation(Input.class);
        boolean required = annotation.required();
        String paramName = StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName();
        if(!paramJson.containsKey(paramName) && required){
            throw new MethodInvokerParamWiredException("not find required param: " + paramName);
        }
        return paramJson.get(paramName);
    }


}
