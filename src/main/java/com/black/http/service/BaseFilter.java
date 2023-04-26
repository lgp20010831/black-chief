package com.black.http.service;

import com.black.http.Request;
import com.black.http.Response;

public class BaseFilter implements Filter{


    @Override
    public void doFilterRequest(FilterChain chain, Request request) throws Throwable {
        chain.fireRequest();
    }

    @Override
    public void doFilterResponse(FilterChain chain, Response response) throws Throwable{
        chain.fireResponse();
    }
}
