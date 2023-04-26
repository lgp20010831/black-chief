package com.black.rpc.inter;

import com.black.rpc.request.PrepareRequest;
import com.black.rpc.request.Request;
import com.black.rpc.RpcRequestType;

import java.io.IOException;

public interface RequestDeserializer {

    boolean support(RpcRequestType requestType);

    Request deserializeRequest(PrepareRequest prepareRequest) throws IOException;

}
