package com.black.core.sql.code.aop;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.core.sql.annotation.UnsupportTransactional;
import org.springframework.core.annotation.AnnotationUtils;

@AopHybrid(SQLPremise.class)
@HybridSort(14501)
public class SQLAopTransactionHybrid implements AopTaskManagerHybrid {

    private SQLTransactionIntercept intercept;

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            return AnnotationUtils.getAnnotation(method, UnsupportTransactional.class) == null &&
                    (AnnotationUtils.getAnnotation(method.getDeclaringClass(), OpenTransactional.class) != null ||
                    AnnotationUtils.getAnnotation(method, OpenTransactional.class) != null);
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (intercept == null){
            intercept = new SQLTransactionIntercept();
        }
        return intercept;
    }
}
