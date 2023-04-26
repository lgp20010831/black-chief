package com.black.rpc.handler.response;

import com.alibaba.fastjson.JSONArray;
import com.black.rpc.response.Response;
import com.black.core.query.MethodWrapper;

public class ArrayResponseHandler extends AbstractResponseHandler{
    @Override
    public boolean support(MethodWrapper mw, Response response) {
        return mw.getReturnType().isArray();
    }

    @Override
    protected Object resolveUtfResponseBody(MethodWrapper mw, String utfBody) {
        JSONArray array = JSONArray.parseArray(utfBody);
        return array.toArray();
    }
}
