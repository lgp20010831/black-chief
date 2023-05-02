package com.black.holder;

import com.black.core.util.Assert;
import com.black.spring.ChiefSpringHodler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

public class SpringHodler {

    private static BeanFactory beanFactory;

    private static ApplicationContext applicationContext;

    public static void setBeanFactory(BeanFactory beanFactory) {
        SpringHodler.beanFactory = beanFactory;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringHodler.applicationContext = applicationContext;
    }


    public static DefaultListableBeanFactory getNonNullListableBeanFactory(){
        DefaultListableBeanFactory listableBeanFactory = getListableBeanFactory();
        Assert.notNull(listableBeanFactory, "bean factory is null");
        return listableBeanFactory;
    }

    public static DefaultListableBeanFactory getListableBeanFactory(){
        return (DefaultListableBeanFactory) getBeanFactory();
    }

    public static void registerSpringBean(String name, Object bean){
        BeanFactory beanFactory = getBeanFactory();
        if (beanFactory != null){
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
            defaultListableBeanFactory.registerSingleton(name, bean);
        }
    }

    public static BeanFactory getBeanFactory() {
        if (beanFactory == null){
            beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        }
        return beanFactory;
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null){
            applicationContext = ChiefSpringHodler.getApplicationContext();
        }
        return applicationContext;
    }
}
