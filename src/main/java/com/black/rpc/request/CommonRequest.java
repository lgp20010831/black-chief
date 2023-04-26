package com.black.rpc.request;

import com.black.rpc.RpcFormat;
import com.black.rpc.RpcRequestType;
import lombok.Setter;

import java.io.IOException;

@Setter
public class CommonRequest implements Request {
    private String methodName;

    private RpcRequestType requestType;

    private String requestId;

    private String actuatorParamBody = "";

    private int bodySize;

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return RpcFormat.createRequestBytes(this);
    }

    @Override
    public Object getParam() {
        return actuatorParamBody;
    }

    @Override
    public int getBodySize() {
        return bodySize;
    }

    @Override
    public void addParam(String newBody) {
        actuatorParamBody += (newBody == null ? "" : newBody);
    }

    @Override
    public RpcRequestType getRequestType() {
        return requestType;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }
}
