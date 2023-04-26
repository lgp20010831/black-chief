package com.black.core.spring.factory;

import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class DefaultAgentObjectBuilderFactory implements AgentObjectBuilderFactory{

    @Override
    public AgentObject createAgentObject(ProxyLayerQueue agentLayerQueue, Class<?> proxyClazz, Object proxy, Method method, MethodProxy methodProxy) {
        return new DefaultAgentObject(proxy, proxyClazz, method, agentLayerQueue, methodProxy);
    }
}
