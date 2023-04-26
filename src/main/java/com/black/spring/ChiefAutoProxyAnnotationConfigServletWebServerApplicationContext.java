package com.black.spring;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Field;

public class ChiefAutoProxyAnnotationConfigServletWebServerApplicationContext extends AnnotationConfigServletWebServerApplicationContext {


    public ChiefAutoProxyAnnotationConfigServletWebServerApplicationContext(){
        try {
            Field beanFactoryField = GenericApplicationContext.class.getDeclaredField("beanFactory");
            beanFactoryField.setAccessible(true);
            ChiefAgencyListableBeanFactory beanFactory = new ChiefAgencyListableBeanFactory();
            beanFactoryField.set(this, beanFactory);
            ChiefSpringHodler.setApplicationContext(this);
            ChiefSpringHodler.setChiefAgencyListableBeanFactory(beanFactory);

        } catch (Throwable e) {
            throw new IllegalStateException("无法增强 bean factoy", e);
        }
    }

}
