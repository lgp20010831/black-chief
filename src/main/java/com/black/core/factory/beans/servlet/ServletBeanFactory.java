package com.black.core.factory.beans.servlet;

import com.black.core.factory.beans.config.AnnotationConfigurationBeanFactory;
import com.black.core.spring.instance.InstanceConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class ServletBeanFactory extends AnnotationConfigurationBeanFactory {

    public ServletBeanFactory() {
    }

    @InstanceConstructor
    public ServletBeanFactory(DefaultListableBeanFactory springFactory) {
        super(springFactory);
    }




}
