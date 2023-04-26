package com.black.http.service;

import com.black.http.Request;

public interface HttpThrowableHandler {

    Object resolveThrowable(Throwable throwable, Request request);
}
