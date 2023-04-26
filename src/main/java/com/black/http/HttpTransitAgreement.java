package com.black.http;

import com.black.utils.IoUtils;

import java.io.IOException;

public interface HttpTransitAgreement {

    default Request parseHttpRequestBytes(byte[] buf) throws IOException {
        return HttpRequestUtils.parseRequest(buf);
    }

    byte[] httpRequestToAim(Request request) throws Throwable;

    Response aimResponseToHttp(byte[] buf) throws Throwable;

    default byte[] toBytesResponse(Response response){
        String responseString = HttpResponseUtils.toResponseString(response);
        return IoUtils.getBytes(responseString, false);
    }
}
