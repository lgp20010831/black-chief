package com.black.rpc.handler.builder;

import com.alibaba.fastjson.JSONObject;
import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.RpcRequestType;
import com.black.rpc.inter.RequestDistinctBuilder;
import com.black.rpc.request.JsonRequest;
import com.black.rpc.request.Request;
import com.black.core.query.MethodWrapper;

import java.io.IOException;
import java.util.UUID;

public class JsonRequestBuilder implements RequestDistinctBuilder {

    @Override
    public boolean support(MethodWrapper mw) {
        return false;
    }

    @Override
    public Request createRequest(MethodWrapper mw, Object requestParam, Object[] rawArgs) throws IOException {
        JsonRequest jsonRequest = new JsonRequest();
        jsonRequest.setRequestType(RpcRequestType.JSON);
        JSONObject requestJson = jsonRequest.getRequestJson();
        requestJson.put(JsonRequest.REQUEST_ID_NAME, UUID.randomUUID().toString());
        requestJson.put(JsonRequest.REQUEST_METHOD_NAME, mw.getName());
        String paramBody = requestParam == null ? "" : requestParam.toString();
        requestJson.put(JsonRequest.REQUEST_BODY_SIZE, DataByteBufferArrayOutputStream.getUtfBytesLen(paramBody));
        requestJson.put(JsonRequest.REQUEST_PARAM, paramBody);
        return jsonRequest;
    }
}
