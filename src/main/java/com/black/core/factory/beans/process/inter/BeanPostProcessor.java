package com.black.core.factory.beans.process.inter;

import com.black.core.factory.beans.BeanFactory;

public interface BeanPostProcessor {


    default void aboutFactory(Object bean, BeanFactory beanFactory){

    }

}
