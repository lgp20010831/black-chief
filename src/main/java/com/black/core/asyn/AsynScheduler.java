package com.black.core.asyn;

import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.code.HijackObject;
import com.black.core.ill.GlobalThrowableCentralizedHandling;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

@AopHybrid  @HybridSort(1120)
public class AsynScheduler implements AopTaskManagerHybrid, AopTaskIntercepet {
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        return new AopMatchTargetClazzAndMethodMutesHandler() {
            @Override
            public boolean matchClazz(Class<?> targetClazz) {
                return true;
            }

            @Override
            public boolean matchMethod(Class<?> targetClazz, Method method) {
                return AnnotationUtils.getAnnotation(method, Async.class) != null &&
                        method.getReturnType().equals(void.class);
            }
        };
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        MethodInvocation invocation = hijack.getInvocation();
        AsynGlobalExecutor.execute(() ->{
            try {
                invocation.proceed();
            } catch (Throwable e) {
                GlobalThrowableCentralizedHandling.resolveThrowable(e);
            }
        });
        return null;
    }
}
