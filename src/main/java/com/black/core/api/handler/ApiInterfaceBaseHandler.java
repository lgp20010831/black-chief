package com.black.core.api.handler;

import java.lang.reflect.Method;
import java.util.List;

public interface ApiInterfaceBaseHandler {

    String getMethodRemark(Class<?> controllerClass, Method method, String alias);

    List<String> getMethodHttpUrls(Class<?> controllerClass, Method method, String alias);

    List<String> getMethodHttpMethod(Class<?> controllerClass, Method method, String alias);
}
