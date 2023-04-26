package com.black.core.event;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.AopApplicationContext;
import com.black.core.aop.code.Premise;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.AnnotationUtils;

public class EventPremise implements Premise {
    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        SpringApplication springApplication = AopApplicationContext.getSpringApplication();
        if (springApplication != null){
            Class<?> mainApplicationClass = springApplication.getMainApplicationClass();
            return AnnotationUtils.getAnnotation(mainApplicationClass, EnableEventAutoDispenser.class) != null;
        }
        return false;
    }
}
