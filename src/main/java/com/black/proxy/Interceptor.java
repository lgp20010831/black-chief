package com.black.proxy;

import java.lang.reflect.Method;

@SuppressWarnings("all")
public interface Interceptor {

    Object invoke(Method method, Object proxy, Object[] args) throws Throwable;

}
