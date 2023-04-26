package com.black.rpc.inter;

import com.alibaba.fastjson.JSONObject;
import com.black.rpc.request.Request;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

public interface JsonRequestHandler {

    boolean support(MethodWrapper mw, ParameterWrapper pw);

    Object wiredParm(MethodWrapper mw, ParameterWrapper pw, JSONObject paramJson, Request request);
}
