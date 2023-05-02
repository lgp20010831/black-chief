package com.black.core.aop.config;

import com.black.core.aop.listener.EnableGlobalAopChainWriedModular;
import com.black.core.spring.ChiefApplicationRunner;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AopConfigurationConditional implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return ChiefApplicationRunner.isPertain(EnableGlobalAopChainWriedModular.class);
    }
}
