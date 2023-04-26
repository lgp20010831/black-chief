package com.black.core.api.handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface ResponseExampleReader {


    String handlerApiResponseExample(Class<?> responseClass, Map<String, String> params, Method method,
                                     Class<?> controllerClass, List<Class<?>> pojoClasses, String example, ExampleStreamAdapter adapter);

}
