package com.black.http.service;

import com.black.http.Request;
import com.black.http.Response;

public class HttpHolder {

    static final ThreadLocal<Request> requestLocal = new ThreadLocal<>();

    static final ThreadLocal<Response> responseLocal = new ThreadLocal<>();


}
