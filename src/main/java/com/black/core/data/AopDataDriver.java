package com.black.core.data;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.*;
import com.black.core.data.annotation.DataMethod;
import com.black.core.data.annotation.EnabledDataTransferStation;
import com.black.core.spring.ChiefApplicationRunner;
import org.springframework.core.annotation.AnnotationUtils;

@AopHybrid(AopDataDriver.class) @HybridSort(4213)
public class AopDataDriver implements AopTaskManagerHybrid, Premise {

    private AopDataIntercept intercept;

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            return AnnotationUtils.getAnnotation(method, DataMethod.class) != null;
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (intercept == null){
            intercept = new AopDataIntercept();
        }
        return intercept;
    }

    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && AnnotationUtils.getAnnotation(mainClass, EnabledDataTransferStation.class) != null;
    }
}
