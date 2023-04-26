package com.black.core.spring.factory;


import com.black.utils.ProxyUtil;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProxyHandler implements InvocationHandler, MethodInterceptor, AgentLayer {
    private final AgentObjectBuilderFactory builderFactory;

    private final Map<Method, AgentObject> agentObjectMap = new HashMap<>();

    private ProxyLayerQueue layerQueue;

    protected AbstractProxyHandler(AgentObjectBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    public void setLayerQueue(ProxyLayerQueue layerQueue) {
        this.layerQueue = layerQueue;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return handler(proxy, method, args, null);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return handler(o, method, objects, methodProxy);
    }

    @Override
    public Object proxy(AgentObject agentObject) {
        if (agentObject.isCGLIB()){
            try {
                return agentObject.getMethodProxy().invokeSuper(agentObject.getProxyObject(), agentObject.getArgs());
            } catch (Throwable throwable) {
                throw new ProxyInvokeException("invoke error", throwable);
            }
        }else {
            try {
                return agentObject.getProxyMethod().invoke(agentObject.getProxyObject(), agentObject.getArgs());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ProxyInvokeException("invoke error", e);
            }
        }
    }

    public Object handler(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (ProxyUtil.isObjectMethod(method)){
            return ProxyUtil.invokeObjectMethod(method, proxy, args, methodProxy);
        }

        if (ProxyUtil.isDefaultMethod(method)){
            return ProxyUtil.invokeDefaultMethod(method, proxy, args);
        }

        AgentObject agentObject;
        if ((agentObject = agentObjectMap.get(method)) == null){
            agentObject = builderFactory.createAgentObject(layerQueue, ProxyUtil.getPrimordialClass(proxy), proxy, method, methodProxy);
        }
        agentObject.clear(args);
        return layerQueue.invoke(agentObject);
    }
}
