package com.black.http.service;

import com.black.http.Request;
import com.black.http.Response;

public interface HttpRequestHandler {

    Response handle(Request request) throws Throwable;


}
