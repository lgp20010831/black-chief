package com.black.core.api.handler;

import java.lang.reflect.Method;

public interface ApiMethodFilter {

    boolean filterApiMethod(Class<?> controllerClass, Method apiMethod);
}
