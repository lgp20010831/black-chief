package com.black.core.sql.code.aop;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.WriedDeleteStatement;
import com.black.core.sql.annotation.WriedRenewStatement;
import com.black.core.sql.annotation.WriedQueryStatement;

@AopHybrid(SQLPremise.class)  @HybridSort(452)
public class SQLWriedWrapperHybrid implements AopTaskManagerHybrid {
    private SQLWrapperMethodIntercept intercept;
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            MethodWrapper methodWrapper = new MethodWrapper(method);
            return methodWrapper.parameterHasAnnotation(WriedQueryStatement.class) ||
                    methodWrapper.parameterHasAnnotation(WriedRenewStatement.class) ||
                    methodWrapper.parameterHasAnnotation(WriedDeleteStatement.class);
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (intercept == null){
            intercept = new SQLWrapperMethodIntercept();
        }
        return intercept;
    }
}
