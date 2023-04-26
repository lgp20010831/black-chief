package com.black.sql_v2.javassist.aop;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.sql_v2.javassist.Agentable;
import org.springframework.core.annotation.AnnotationUtils;

@AopHybrid
@HybridSort(87772)
public class SqlV2AopHybrid implements AopTaskManagerHybrid {
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            return AnnotationUtils.getAnnotation(method, Agentable.class) != null;
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return SqlV2AopIntercept.getInstance();
    }
}
