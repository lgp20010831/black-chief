package com.black.rpc.handler;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.request.CommonRequest;
import com.black.rpc.request.Request;
import com.black.rpc.RpcRequestType;
import com.black.rpc.inter.RequestDistinctBuilder;
import com.black.core.query.MethodWrapper;

import java.util.UUID;

public class DefaultRequestBuildler implements RequestDistinctBuilder {
    @Override
    public boolean support(MethodWrapper mw) {
        return true;
    }

    @Override
    public Request createRequest(MethodWrapper mw, Object requestParam, Object[] rawArgs) {
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setRequestType(RpcRequestType.COMMON);
        commonRequest.setRequestId(UUID.randomUUID().toString());
        commonRequest.setMethodName(mw.getName());
        String paramBody = requestParam == null ? "" : requestParam.toString();
        commonRequest.setBodySize(DataByteBufferArrayOutputStream.getUtfBytesLen(paramBody));
        commonRequest.setActuatorParamBody(paramBody);
        return commonRequest;
    }


}
