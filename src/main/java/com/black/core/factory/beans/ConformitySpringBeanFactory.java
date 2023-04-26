package com.black.core.factory.beans;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class ConformitySpringBeanFactory extends DefaultBeanFactory {

    private final DefaultListableBeanFactory springFactory;

    public ConformitySpringBeanFactory(DefaultListableBeanFactory springFactory) {
        this.springFactory = springFactory;
    }

    @Override
    public void autoWriedBean(Object bean) {
        super.autoWriedBean(bean);
        if (springFactory != null){
            springFactory.autowireBean(bean);
        }
    }
}
