package com.black.core.aop.ibatis;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.AopApplicationContext;
import com.black.core.aop.code.Premise;
import com.black.core.mybatis.source.annotation.EnableDynamicallyMultipleClients;
import org.springframework.boot.SpringApplication;
import org.springframework.core.annotation.AnnotationUtils;

public class IbatisPremise implements Premise {

    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        SpringApplication springApplication = AopApplicationContext.getSpringApplication();
        if (springApplication != null){
            Class<?> mainApplicationClass = springApplication.getMainApplicationClass();
            if (mainApplicationClass != null){
                return AnnotationUtils.findAnnotation(mainApplicationClass, EnableDynamicallyMultipleClients.class) != null;
            }
        }
        return false;
    }
}
