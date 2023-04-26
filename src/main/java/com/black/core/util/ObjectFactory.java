package com.black.core.util;



import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

@SuppressWarnings("all")
public final class ObjectFactory {

    /* 禁止创建 */
    private ObjectFactory(){throw new Error("请使用静态方法调用");}

    /* create object */
    public static <O> O newObject(Class<O> targetClass){
        return newObject(targetClass,null);
    }

    public static <O> O newObject(Class<O> targetClass, Class<?>[] paramTypes, Object... args) {
        Constructor<O> constructor = null;
        try {
            constructor = targetClass.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /* create jdk proxy */
    public static <T> T newJDKProxy(Class<T> interfaceClass, InvocationHandler handler){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class[]{interfaceClass},handler);
    }

    /* create CGlib Proxy */
    public static <G> G newCGlibProxy(Class<G> superClass, MethodInterceptor interceptor){
        return newCGlibProxy(superClass, interceptor,null);
    }

    public static <G> G newCGlibProxy(Class<G> superClass, MethodInterceptor interceptor,
                                      Class<?>[] paramTypes, Object... args){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(superClass);
        enhancer.setCallback(interceptor);
        return paramTypes != null ? (G) enhancer.create(paramTypes, args) : (G) enhancer.create();
    }
}
