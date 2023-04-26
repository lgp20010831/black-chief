package com.black.core.aop.code;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PitchClassWithMethodsWrapperFactory {

    private final Map<AopTaskManagerHybrid, PitchClassWithMethodsWrapper> wrapperCache = new HashMap<>();

    public PitchClassWithMethodsWrapper createWrapper(Class<?> clazz, AopTaskManagerHybrid hybrid){
        return createWrapper(clazz, null, hybrid);
    }

    public PitchClassWithMethodsWrapper createWrapper(Class<?> clazz, Method pointMethod, AopTaskManagerHybrid hybrid){
        PitchClassWithMethodsWrapper wrapper = wrapperCache.computeIfAbsent(hybrid, h -> new PitchClassWithMethodsWrapper(clazz));
        if (pointMethod != null){
            wrapper.addMethod(pointMethod);
            wrapper.setPointMethod(pointMethod);
        }
        return wrapper;
    }
}
