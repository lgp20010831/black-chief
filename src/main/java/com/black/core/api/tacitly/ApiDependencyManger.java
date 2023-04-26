package com.black.core.api.tacitly;

import com.black.core.api.ApiHttpRestConfigurer;
import com.black.core.api.handler.DependencyRegister;
import com.black.core.spring.util.ApplicationUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ApiDependencyManger {

    private final Map<Class<?>, List<Class<?>>> dependencyMap = new HashMap<>();

    private final ApiAliasManger aliasManger;
    private final ApiHttpRestConfigurer apiHttpRestConfigurer;
    private final DefaultDependencyRegister defaultDependencyRegister;

    public ApiDependencyManger(ApiAliasManger aliasManger, ApiHttpRestConfigurer apiHttpRestConfigurer) {
        this.aliasManger = aliasManger;
        this.apiHttpRestConfigurer = apiHttpRestConfigurer;
        defaultDependencyRegister = new DefaultDependencyRegister();
    }

    public void refreshDependencyRelationship(){
        final Map<String, Class<?>> controllerAliasMap = aliasManger.getControllerAliasMap();
        final Map<String, Class<?>> pojoALiasMap = aliasManger.getPojoALiasMap();
        controllerAliasMap.forEach((a, c) ->{
            apiHttpRestConfigurer.handlerDependency(c, a, pojoALiasMap, defaultDependencyRegister);
            dependencyMap.put(c, defaultDependencyRegister.getDependencyList()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            defaultDependencyRegister.clear();

        });
    }

    public Map<Class<?>, List<Class<?>>> getDependencyMap() {
        return dependencyMap;
    }

    public ApiAliasManger getAliasManger() {
        return aliasManger;
    }

    public static class DefaultDependencyRegister implements DependencyRegister{

        private final List<Class<?>> dependencyList = new ArrayList<>();

        @Override
        public void register(Class<?>... pojoDependency) {
            if (pojoDependency != null) {
                dependencyList.addAll(Arrays.asList(pojoDependency));
            }
        }

        public List<Class<?>> getDependencyList() {
            return (List<Class<?>>) ApplicationUtil.clone(dependencyList);
        }

        public void clear(){
            dependencyList.clear();
        }
    }
}
