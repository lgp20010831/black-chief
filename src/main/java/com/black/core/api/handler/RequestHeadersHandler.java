package com.black.core.api.handler;

import java.lang.reflect.Method;
import java.util.Map;

public interface RequestHeadersHandler {


    void obtainRequestHeaders(Class<?> controllerClass, Method method, String alias, Map<String, String> handlers);

}
