package com.black.core.spring.factory;

import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public interface AgentObjectBuilderFactory {

    AgentObject createAgentObject(ProxyLayerQueue agentLayerQueue, Class<?> proxyClazz, Object proxy, Method method, MethodProxy methodProxy);
}
