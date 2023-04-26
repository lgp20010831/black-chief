package com.black.core.aop.code;


import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalAopCollectPointCut implements Pointcut {

    private final Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> matchHybridCache;


    private final AopGlobalAdvisor globalAdvisor;

    public GlobalAopCollectPointCut(Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> matchHybridCache,
                                    AopGlobalAdvisor globalAdvisor) {
        this.matchHybridCache = matchHybridCache;
        this.globalAdvisor = globalAdvisor;
    }

    public Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> getMatchHybridCache() {
        return matchHybridCache;
    }

    @Override
    public ClassFilter getClassFilter() {
        return clazz -> {
            AtomicBoolean result = new AtomicBoolean(false);
            if (matchHybridCache != null){
                matchHybridCache.forEach((hybrid, matcher) ->{
                    if (matcher.matchClazz(clazz)) {
                        result.set(true);
                        globalAdvisor.successfulMatchClass(clazz, hybrid);
                    }
                });
            }
            return result.get();
        };
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new MethodMatcher() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                AtomicBoolean result = new AtomicBoolean(false);
                if (matchHybridCache != null){
                    matchHybridCache.forEach((hybrid, matcher) ->{
                        if (matcher.matchMethod(targetClass, method)) {
                            result.set(true);
                            globalAdvisor.successfulMatchMethod(targetClass, method, hybrid);
                        }
                    });
                }
                return result.get();
            }

            @Override
            public boolean isRuntime() {
                return false;
            }

            @Override
            public boolean matches(Method method, Class<?> targetClass, Object... args) {
                return false;
            }
        };
    }
}
