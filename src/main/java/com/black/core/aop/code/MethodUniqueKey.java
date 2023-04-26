package com.black.core.aop.code;

import java.lang.reflect.Method;
import java.util.Objects;

public class MethodUniqueKey {

    private final Method method;

    private final Class<?> targetClazz;

    public MethodUniqueKey(Method method, Class<?> targetClazz) {
        this.method = method;
        this.targetClazz = targetClazz;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getTargetClazz() {
        return targetClazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodUniqueKey that = (MethodUniqueKey) o;
        return Objects.equals(method, that.method) && Objects.equals(targetClazz, that.targetClazz);
    }

    @Override
    public int hashCode() {
        return method.getName().hashCode() ^ targetClazz.getName().hashCode();
    }
}
