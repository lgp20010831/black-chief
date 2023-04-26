package com.black.core.aop.servlet;

import javax.servlet.http.HttpServletRequest;


public class ServletRequestHodler {

    private final HttpServletRequest request;

    private static HttpServletRequest servletRequest;

    public ServletRequestHodler(HttpServletRequest request){
        this.request = request;
        servletRequest = request;
    }

    public static HttpServletRequest getServletRequest() {
        return servletRequest;
    }
}
