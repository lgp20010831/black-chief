package com.black.core.aop.servlet;

public class RestResponseLocal {


    private static ThreadLocal<Class<? extends RestResponse>> responseTypeLocal = new ThreadLocal<>();

    public static void setType(Class<? extends RestResponse> type){
        responseTypeLocal.set(type);
    }

    public static Class<? extends RestResponse> getType(){
        return responseTypeLocal.get();
    }

    public static void closeType(){
        responseTypeLocal.remove();
    }
}
