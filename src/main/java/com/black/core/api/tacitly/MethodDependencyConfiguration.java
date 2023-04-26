package com.black.core.api.tacitly;

import java.lang.reflect.Method;

public class MethodDependencyConfiguration {


    private final Method method;

    private final Class<?> superClass;

    private final ApiDependencyManger dependencyManger;

    public MethodDependencyConfiguration(Method method, Class<?> superClass, ApiDependencyManger dependencyManger) {
        this.method = method;
        this.superClass = superClass;
        this.dependencyManger = dependencyManger;
    }





}
