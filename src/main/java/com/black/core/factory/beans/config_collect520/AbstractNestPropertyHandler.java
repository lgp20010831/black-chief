package com.black.core.factory.beans.config_collect520;

import com.black.core.factory.beans.BeanFactory;

@SuppressWarnings("all")
public abstract class AbstractNestPropertyHandler {

    protected void handlerTarget(Object value, BeanFactory factory){
        if (!(factory instanceof AttributeInjectionEnhancementBeanFactory)){
            throw new IllegalStateException("current factory is not AttributeInjectionEnhancementBeanFactory");
        }

        AttributeInjectionEnhancementBeanFactory enhancementBeanFactory = (AttributeInjectionEnhancementBeanFactory) factory;
        enhancementBeanFactory.pourinto(value);
    }

}
