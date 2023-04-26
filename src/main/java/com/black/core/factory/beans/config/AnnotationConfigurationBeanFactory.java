package com.black.core.factory.beans.config;

import com.black.core.factory.beans.ApplicationConfigurationMethodHandler;
import com.black.core.factory.beans.ConformitySpringBeanFactory;
import com.black.core.factory.beans.yml.YmlBeanPostProcessor;
import com.black.core.spring.instance.InstanceConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class AnnotationConfigurationBeanFactory extends ConformitySpringBeanFactory {

    public AnnotationConfigurationBeanFactory(){
        this(null);
    }

    @InstanceConstructor
    public AnnotationConfigurationBeanFactory(DefaultListableBeanFactory springFactory) {
        super(springFactory);
        registerBeanLifeCycleProcessor(new ConfigurationBeanPostProcessor());
        registerBeanFactoryProcessor(new YmlBeanPostProcessor());
        registerBeanFactoryProcessor(new ApplicationConfigurationMethodHandler());
        registerBeanFactoryProcessor(new DefaultValueBeanMethodHandler());
        registerBeanFactoryProcessor(new YmlPropertiesMethodHandler());
        registerBeanFactoryProcessor(new PropertiesBeanParamHandler());
    }

}
