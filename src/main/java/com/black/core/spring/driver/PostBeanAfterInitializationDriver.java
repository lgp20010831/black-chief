package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;

public interface PostBeanAfterInitializationDriver extends Driver {

    default Object postProcessAfterInitialization(Object bean, String beanName, ChiefExpansivelyApplication chiefExpansivelyApplication){
        return bean;
    }
}
