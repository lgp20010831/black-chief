package com.black.core.aop.servlet;

public class HttpMethodManager {

    private static final ThreadLocal<HttpMethodWrapper> httpMethodLocal = new ThreadLocal<>();

    public static void save(HttpMethodWrapper mw){
        httpMethodLocal.set(mw);
    }

    public static HttpMethodWrapper getHttpMethod(){
        return httpMethodLocal.get();
    }

    public static void close(){
        httpMethodLocal.remove();
    }
}
