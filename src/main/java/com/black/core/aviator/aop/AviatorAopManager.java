package com.black.core.aviator.aop;

import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aviator.AviatorPremise;
import com.black.core.aviator.annotation.AopAviator;

import java.lang.reflect.Method;

@AopHybrid(AviatorPremise.class)
@HybridSort(15467)
public class AviatorAopManager implements AopTaskManagerHybrid {

    private AviatorHijackLayer hijackLayer;

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        return new AopMatchTargetClazzAndMethodMutesHandler() {
            @Override
            public boolean matchClazz(Class<?> targetClazz) {
                return targetClazz.isAnnotationPresent(AopAviator.class);
            }

            @Override
            public boolean matchMethod(Class<?> targetClazz, Method method) {
                return targetClazz.isAnnotationPresent(AopAviator.class);
            }
        };
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (hijackLayer == null){
            hijackLayer = new AviatorHijackLayer();
        }
        return hijackLayer;
    }
}
