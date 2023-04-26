package com.black.core.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

public class ApplicationHolder {

    protected static ApplicationContext applicationContext;

    protected static BeanFactory beanFactory;

    public ApplicationHolder(ApplicationContext applicationContext, BeanFactory beanFactory) {
        ApplicationHolder.applicationContext = applicationContext;
        ApplicationHolder.beanFactory = beanFactory;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
