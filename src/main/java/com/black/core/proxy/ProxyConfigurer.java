package com.black.core.proxy;

import com.black.core.spring.factory.AgentLayer;

import java.util.Collection;
import java.util.Map;

public interface ProxyConfigurer {

    Map<AgentLayer, Collection<String>> packageAgent(ProxyConfigurerAdapter configurerAdapter);

    Map<AgentLayer, Collection<TargetWrapper>> registerTarget(ProxyConfigurerAdapter configurerAdapter);
}
