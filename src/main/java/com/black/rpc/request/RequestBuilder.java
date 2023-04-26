package com.black.rpc.request;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.RpcRequestType;

import java.util.UUID;

public class RequestBuilder {


    public static Request fetch(String name, Object param){
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setRequestType(RpcRequestType.COMMON);
        commonRequest.setRequestId(UUID.randomUUID().toString());
        commonRequest.setMethodName(name);
        int utfBytesLen = DataByteBufferArrayOutputStream.getUtfBytesLen(param);
        commonRequest.setBodySize(utfBytesLen);
        commonRequest.setActuatorParamBody(param == null ? "" : param.toString());
        return commonRequest;
    }


}
