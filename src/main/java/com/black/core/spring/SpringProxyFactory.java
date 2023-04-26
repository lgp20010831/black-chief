package com.black.core.spring;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;


public interface SpringProxyFactory {

    Logger log = LoggerFactory.getLogger(SpringProxyFactory.class);




    default Object[] returnArgs(Constructor<?> constructor, BeanFactory beanFactory){
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            try {
                Object bean = beanFactory.getBean(parameters[i].getType());
                args[i] = bean;
            }catch (BeansException ex){
                if (log.isWarnEnabled()) {
                    log.warn("The required constructor parameter could not" +
                                    " be found from the container, constructor: {}, parameter type: {}",
                            constructor, parameters[i].getType());
                }

                args[i] = null;
            }
        }
        return args;
    }
}
