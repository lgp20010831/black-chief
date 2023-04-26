package com.black.core.aop.servlet.security;

import com.black.core.aop.servlet.RestResponse;
import com.black.core.mvc.response.Response;

public class DefaultSecurityResponse implements SecurityResponseHandler{
    @Override
    public RestResponse getResponse() {
        Response response = new Response();
        response.setCode(407);
        response.setMessage("无访问权限!!!");
        response.setSuccessful(false);
        return response;
    }
}
