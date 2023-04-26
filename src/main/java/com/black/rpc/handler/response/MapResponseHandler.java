package com.black.rpc.handler.response;

import com.alibaba.fastjson.JSONObject;
import com.black.rpc.response.Response;

import com.black.core.query.MethodWrapper;

import java.util.Map;

public class MapResponseHandler extends AbstractResponseHandler{

    @Override
    public boolean support(MethodWrapper mw, Response response) {
        return Map.class.isAssignableFrom(mw.getReturnType());
    }

    @Override
    protected Object resolveUtfResponseBody(MethodWrapper mw, String utfBody) {
        return JSONObject.parseObject(utfBody);
    }
}
