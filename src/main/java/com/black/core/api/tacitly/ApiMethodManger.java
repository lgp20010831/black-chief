package com.black.core.api.tacitly;

import com.black.core.api.ApiHttpRestConfigurer;
import com.black.core.api.annotation.ApiDescribe;
import com.black.core.api.handler.ApiMethodCollector;
import com.black.core.api.handler.ApiMethodFilter;
import com.black.core.api.handler.ItemResolutionModule;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;

public class ApiMethodManger {

    private final ApiAliasManger aliasManger;

    private final ApiHttpRestConfigurer configurer;

    private final ApiDependencyManger dependencyManger;

    private final Collection<ApiMethodFilter> apiMethodFilters = new ArrayList<>();

    private final Map<Class<?>, List<Method>> collectApiMethods = new HashMap<>();

    private final Map<Method, ItemResolutionModule> responseResolutionModuleMap = new HashMap<>();

    private final Map<Method, ItemResolutionModule> requestResolutionModuleMap = new HashMap<>();

    public ApiMethodManger(ApiAliasManger aliasManger, ApiHttpRestConfigurer configurer, ApiDependencyManger dependencyManger) {
        this.aliasManger = aliasManger;
        this.configurer = configurer;
        this.dependencyManger = dependencyManger;
    }

    public void collectApiMethod(){
        //获取方法收集器
        ApiMethodCollector apiMethodCollector = configurer.registerApiMethodCollector();
        //注册方法过滤器
        configurer.registerApiMethodFilter(apiMethodFilters);
        for (Class<?> value : aliasManger.getControllerAliasMap().values()) {

            //通过收集器收集的所有方法
            Map<Class<?>, List<Method>> collectApiMethods = apiMethodCollector.collectApiMethods(value);
            collectApiMethods.forEach((c, ms) ->{

                Iterator<Method> iterable = ms.iterator();
                got: while (iterable.hasNext()) {
                    Method method = iterable.next();
                    for (ApiMethodFilter filter : apiMethodFilters) {
                        if (!filter.filterApiMethod(c, method)) {
                            iterable.remove();
                            continue got;
                        }
                    }
                }
            });
            this.collectApiMethods.putAll(collectApiMethods);
        }

        //将最后收集到的方法存到 collectApiMethods 中
        collectApiMethods.forEach((c, ms) ->{
            for (Method method : ms) {
                ApiDescribe describe = AnnotationUtils.getAnnotation(method, ApiDescribe.class);
                requestResolutionModuleMap.put(method,
                        new DefaultItemResolution(null).parse(describe == null ? null : describe.requestDescribe(), c, dependencyManger));

                responseResolutionModuleMap.put(method,
                        new DefaultItemResolution(null).parse(describe == null ? null : describe.responseDescribe(), c, dependencyManger));
            }
        });
    }

    public void clear(){
        collectApiMethods.clear();
    }

    public Map<Class<?>, List<Method>> getCollectApiMethods() {
        return collectApiMethods;
    }

    public Map<Method, ItemResolutionModule> getRequestResolutionModuleMap() {
        return requestResolutionModuleMap;
    }

    public Map<Method, ItemResolutionModule> getResponseResolutionModuleMap() {
        return responseResolutionModuleMap;
    }
}
