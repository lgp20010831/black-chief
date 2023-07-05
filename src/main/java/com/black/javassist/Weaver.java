package com.black.javassist;

import java.lang.reflect.Method;

@SuppressWarnings("all")
public interface Weaver {

    void braid(Method method, Object target, Object[] args) throws Throwable;

}
