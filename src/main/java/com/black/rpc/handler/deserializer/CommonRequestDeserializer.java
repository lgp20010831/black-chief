package com.black.rpc.handler.deserializer;

import com.black.rpc.request.CommonRequest;
import com.black.rpc.request.PrepareRequest;
import com.black.rpc.request.Request;
import com.black.rpc.RpcRequestType;
import com.black.rpc.inter.RequestDeserializer;

import java.io.IOException;


public class CommonRequestDeserializer implements RequestDeserializer {

    @Override
    public boolean support(RpcRequestType requestType) {
        return requestType == RpcRequestType.COMMON;
    }

    @Override
    public Request deserializeRequest(PrepareRequest prepareRequest) throws IOException {
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setRequestType(RpcRequestType.typeOf(prepareRequest.getType()));
        commonRequest.setRequestId(prepareRequest.getRequestId());
        commonRequest.setMethodName(prepareRequest.getMethodName());
        commonRequest.setBodySize(prepareRequest.getBodySize());
        commonRequest.setActuatorParamBody(prepareRequest.getBodyInput().readUnrestrictedUtf());
        return commonRequest;
    }


}
