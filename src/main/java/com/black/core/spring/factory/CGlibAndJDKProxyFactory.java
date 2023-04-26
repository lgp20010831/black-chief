package com.black.core.spring.factory;

import com.black.core.json.NotNull;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public interface CGlibAndJDKProxyFactory {

    default <T> T proxyJDK(@NotNull Class<T> target, InvocationHandler invocationHandler){
        return (T) Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, invocationHandler);
    }

    default <G> G proxyCGlib(@NotNull Class<G> target, MethodInterceptor methodInterceptor){
        return proxyCGlib(target, methodInterceptor, null);
    }

    default <G> G proxyCGlib(@NotNull Class<G> target, MethodInterceptor methodInterceptor, Class<?>[] paramTypes, Object... args){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setCallback(methodInterceptor);
        return paramTypes != null ? (G) enhancer.create(paramTypes, args) : (G) enhancer.create();
    }
}
