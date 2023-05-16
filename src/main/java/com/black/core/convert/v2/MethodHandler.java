package com.black.core.convert.v2;

import com.black.core.query.ClassWrapper;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author 李桂鹏
 * @create 2023-05-16 16:12
 */
@SuppressWarnings("all") @Data
public class MethodHandler {

    private final Method method;

    private final Object instance;

    private final Class<?> paramType;

    public MethodHandler(Method method, Object instance, Class<?> paramType) {
        this.method = method;
        this.instance = instance;
        this.paramType = paramType;
    }

    public Object invoke(Object arg){
        try {
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers())){
                return method.invoke(null, arg);
            }else {
                return method.invoke(instance, arg);
            }
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
    }

    public boolean supportConvert(Class<?> type){
        Class<?> returnType = method.getReturnType();
        return ClassWrapper.autoAssemblyAndDisassembly(returnType, type) || type.isAssignableFrom(returnType);
    }

    public boolean supportParam(Class<?> type){
        return ClassWrapper.autoAssemblyAndDisassembly(paramType, type) || paramType.isAssignableFrom(type);

    }
}
