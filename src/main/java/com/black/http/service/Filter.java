package com.black.http.service;

import com.black.http.Request;
import com.black.http.Response;

public interface Filter {

    void doFilterRequest(FilterChain chain, Request request) throws Throwable;


    void doFilterResponse(FilterChain chain, Response response)  throws Throwable;
}
