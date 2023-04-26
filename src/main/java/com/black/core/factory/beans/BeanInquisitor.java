package com.black.core.factory.beans;

import com.black.core.factory.beans.process.inter.BeanPostProcessor;

// bean检察官
public interface BeanInquisitor extends BeanPostProcessor {

    default void inspectInstance(Object bean, BeanDefinitional<?> definitional, BeanFactory beanFactory){

    }

    default void inspectInitialize(Object bean, BeanDefinitional<?> definitional, BeanFactory beanFactory){

    }
}
