package com.black.core.proxy;

import com.black.core.spring.factory.AgentLayer;

import java.util.*;

public class AutomaticProxyInjectionConfiguration {

    private final Map<AgentLayer, Collection<TargetWrapper>> proxyCache = new HashMap<>(16);

    private final Map<AgentLayer, Collection<String>> scopes = new HashMap<>(16);

    public final AutomaticProxyInjectionConfiguration packageAgent(String[] packages, AgentLayer... layer){
        for (AgentLayer agentLayer : layer) {
            Collection<String> scope = scopes.computeIfAbsent(agentLayer, key -> new ArrayList<>());
            scope.addAll(Arrays.asList(packages));
        }
        return this;
    }

    public final AutomaticProxyInjectionConfiguration registerTarget(Collection<TargetWrapper> target, AgentLayer... layer){
        for (AgentLayer agentLayer : layer) {
            Collection<TargetWrapper> scope = proxyCache.computeIfAbsent(agentLayer, key -> new ArrayList<>());
            scope.addAll(target);
        }
        return this;
    }

    public void putAllTarget(Map<AgentLayer, Collection<TargetWrapper>> source){
        if (source != null){
            proxyCache.putAll(source);
        }
    }

    public void putAllCache(Map<AgentLayer, Collection<String>> targetSource){
        if (targetSource != null){
            scopes.putAll(targetSource);
        }
    }

    public Map<AgentLayer, Collection<String>> getScopes() {
        return scopes;
    }

    public Map<AgentLayer, Collection<TargetWrapper>> getProxyCache() {
        return proxyCache;
    }
}
