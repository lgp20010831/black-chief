package com.black.core.sql.code.aop;

import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;

public class SQLMethodCache {

    public static MethodWrapper getWrapper(Method method){
        return MethodWrapper.get(method);
    }
}
