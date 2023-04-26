package com.black.core.aop.ibatis;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.*;
import org.springframework.core.annotation.AnnotationUtils;
import com.black.core.mybatis.source.annotation.*;

@AopHybrid(IbatisPremise.class)  @HybridSort(500)
public class AopIbatisRollBackHybrid implements AopTaskManagerHybrid {

    private IbatisTransactionIntercept transactionIntercept;

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            return AnnotationUtils.getAnnotation(method.getDeclaringClass(), DynamicallySimpleRollBackTransactional.class) != null ||
                    AnnotationUtils.getAnnotation(method, DynamicallySimpleRollBackTransactional.class) != null;
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (transactionIntercept == null){
            transactionIntercept = new IbatisTransactionIntercept();
        }
        return transactionIntercept;
    }
}
