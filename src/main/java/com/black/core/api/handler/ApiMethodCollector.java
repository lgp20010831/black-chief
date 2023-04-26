package com.black.core.api.handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface ApiMethodCollector {


    Map<Class<?>, List<Method>> collectApiMethods(Class<?> controllerClass);

}
