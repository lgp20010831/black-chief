package com.black.core.config;

import com.black.holder.SpringHodler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public abstract class AbstractConfiguration implements BeanFactoryAware, InitializingBean,
         ApplicationContextAware{

    protected BeanFactory beanFactory;

    protected ApplicationContext applicationContext;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        SpringHodler.setBeanFactory(beanFactory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringHodler.setApplicationContext(applicationContext);
    }

}
