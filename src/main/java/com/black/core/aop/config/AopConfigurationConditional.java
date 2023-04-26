package com.black.core.aop.config;

import com.black.core.aop.listener.EnableGlobalAopChainWriedModular;
import com.black.core.aop.listener.SpringApplicationStaticHodler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AopConfigurationConditional implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        SpringApplication springApplication = SpringApplicationStaticHodler.getSpringApplication();
        if (springApplication != null){
            Class<?> mainApplicationClass = springApplication.getMainApplicationClass();
            return AnnotationUtils.getAnnotation(mainApplicationClass, EnableGlobalAopChainWriedModular.class) != null;
        }
        return false;
    }
}
