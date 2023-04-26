package com.black.rpc.handler.param;


import com.alibaba.fastjson.JSONObject;
import com.black.rpc.annotation.Output;
import com.black.rpc.handler.RequestParamCarrier;
import com.black.rpc.ill.MethodInvokerParamWiredException;
import com.black.rpc.inter.RequestParamResolver;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;

public class OutputParamResolver implements RequestParamResolver {
    @Override
    public boolean support(MethodWrapper mw, ParameterWrapper pw) {
        return pw.hasAnnotation(Output.class);
    }

    @Override
    public void resolverParam(MethodWrapper mw, ParameterWrapper pw, Object param, RequestParamCarrier rpc) {
        Output annotation = pw.getAnnotation(Output.class);
        boolean required = annotation.required();
        String paramName = StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName();
        if (required && param == null){
            throw new MethodInvokerParamWiredException("out put param: " + paramName + " is required");
        }
        JSONObject requestJson = rpc.getRequestJson();
        requestJson.put(paramName, param);
    }
}
