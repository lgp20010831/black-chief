package com.black.rpc.request;

import com.black.rpc.RpcRequestType;

public abstract class AbstractBaseRequest implements Request{

    protected RpcRequestType requestType;

    @Override
    public RpcRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RpcRequestType requestType) {
        this.requestType = requestType;
    }
}
