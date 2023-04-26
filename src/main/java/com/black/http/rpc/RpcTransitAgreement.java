package com.black.http.rpc;

import com.black.http.*;

public class RpcTransitAgreement implements HttpTransitAgreement {

    @Override
    public byte[] httpRequestToAim(Request request) throws Throwable {
        return HttpRpcTransitDevice.castHttpRequestToRpcRequest0(request);
    }

    @Override
    public Response aimResponseToHttp(byte[] buf) throws Throwable {
        return HttpRpcTransitDevice.castRpcResponseToHttpResponse2(buf);
    }

}
