package com.black.core.ill.aop;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.*;

import com.black.core.spring.ChiefApplicationRunner;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AopHybrid(IllAopHybrid.class) @HybridSort(650)
public class IllAopHybrid implements AopTaskManagerHybrid, Premise {
    private IllIntercept illIntercept;
    private final Map<Method, ThrowableCaptureConfiguration> throwableConfigurationMutes = new ConcurrentHashMap<>();
    public IllAopHybrid() {

    }
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            GlobalThrowableCaptureIntercept annotation = AnnotationUtils.getAnnotation(method, GlobalThrowableCaptureIntercept.class);
            boolean proxy = annotation != null;
            if (proxy){
                ThrowableCaptureConfiguration configuration = new ThrowableCaptureConfiguration(method);
                com.black.core.util.AnnotationUtils.loadAttribute(annotation, configuration);
                throwableConfigurationMutes.put(method, configuration);
            }
            return proxy;
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (illIntercept == null){
            illIntercept = new IllIntercept(this);
        }
        return illIntercept;
    }

    public Map<Method, ThrowableCaptureConfiguration> getThrowableConfigurationMutes() {
        return throwableConfigurationMutes;
    }

    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        return ChiefApplicationRunner.isPertain(EnabledGlobalThrowableManagement.class);
    }
}
