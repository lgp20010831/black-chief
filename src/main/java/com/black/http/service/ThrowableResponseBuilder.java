package com.black.http.service;

import com.black.http.HttpResponseUtils;
import com.black.http.Response;
import com.black.utils.ServiceUtils;

public class ThrowableResponseBuilder {

    private final HttpConfiguration configuration;

    public ThrowableResponseBuilder(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    public Response build(Throwable throwable, Object result){
        String message = ServiceUtils.getThrowableMessage(throwable, "system error");
        String str = result == null ? "" : result.toString();
        return HttpResponseUtils.createResponse(str, 500, message, "application/json");
    }

}
