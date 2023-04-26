package com.black.core.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TypeMethodWrapper {

    private final Method method;

    private final Object target;

    private final String entry;

    public TypeMethodWrapper(Method method, Object target, String entry) {
        this.method = method;
        this.target = target;
        this.entry = entry;
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public String getEntry() {
        return entry;
    }

    public Object invoke(Object arg){
        try {
            return method.invoke(target, arg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
