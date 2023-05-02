package com.black.core.aviator.component;

import com.black.aviator.AviatorManager;
import com.black.core.aviator.annotation.AviatorFunction;
import com.black.core.aviator.annotation.EnabledGlobalAviatorLayer;
import com.black.core.chain.*;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.function.AbstractFunction;

import java.util.HashSet;
import java.util.Set;

@LoadSort(94)
@ChainClient(AviatorFunctionCollector.class)
@LazyLoading(EnabledGlobalAviatorLayer.class)
public class AviatorFunctionCollector implements OpenComponent, CollectedCilent, ChainPremise {

    Set<AbstractFunction> functionSet = new HashSet<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        AviatorEvaluatorInstance instance = AviatorManager.getInstance();
        for (AbstractFunction function : functionSet) {
            instance.addFunction(function);
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("fun", cli -> {
            return BeanUtil.isSolidClass(cli) && AbstractFunction.class.isAssignableFrom(cli) && cli.isAnnotationPresent(AviatorFunction.class);
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        for (Object obj : resultBody.getCollectSource()) {
            functionSet.add((AbstractFunction) obj);
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledGlobalAviatorLayer.class);
    }
}
