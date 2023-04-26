package com.black.core.proxy;

import com.black.core.spring.factory.AgentLayer;
import lombok.NonNull;

import java.util.*;

public class ProxyConfigurerAdapter {

     public RegisterAgents point(AgentLayer... agentLayer){
        return new RegisterAgents(Arrays.asList(agentLayer));
     }

    public static class RegisterAgents{

        private final Collection<AgentLayer> agentLayers;

        private final Map<AgentLayer, Collection<TargetWrapper>> targetClazzMap = new HashMap<>();
        public RegisterAgents(Collection<AgentLayer> agentLayers) {
            this.agentLayers = agentLayers;
        }

        public Map<AgentLayer, Collection<String>> pointPackage(String[] packages){
            Map<AgentLayer, Collection<String>> target = new HashMap<>();
            for (AgentLayer agentLayer : agentLayers) {
                target.put(agentLayer, Arrays.asList(packages));
            }
            return target;

        }

        public RegisterAgents registerTargetClazz(@NonNull Class<?> targetClazz, boolean forSpring){
            for (AgentLayer agentLayer : agentLayers) {
                Collection<TargetWrapper> targetWrappers = targetClazzMap.computeIfAbsent(agentLayer, key -> new ArrayList<>());
                targetWrappers.add(new TargetWrapper(forSpring, targetClazz));
            }
            return this;
        }

        public Map<AgentLayer, Collection<TargetWrapper>> propose(){
            return targetClazzMap;
        }
    }
}
