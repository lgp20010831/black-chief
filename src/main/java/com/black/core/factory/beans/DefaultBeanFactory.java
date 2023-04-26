package com.black.core.factory.beans;

import com.black.core.factory.beans.process.impl.PrototypeBeanMethodParamHandler;
import com.black.core.factory.beans.process.impl.PrototypeFieldResolver;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.factory.ReusingProxyFactory;

public class DefaultBeanFactory extends AbstractBeanFactory{

    public DefaultBeanFactory(){
        registerBeanFactoryProcessor(new WiredBeanFactoryProcessor());
        registerBeanFactoryProcessor(new DefaultBeanMethodHandler());
        registerBeanFactoryProcessor(new PrototypeBeanMethodParamHandler());
        registerBeanFactoryProcessor(new PrototypeFieldResolver());
        registerBean(this);
    }

    @Override
    public ReusingProxyFactory getProxyFactory() {
        FactoryManager.createDefaultProxyFactory();
        return FactoryManager.getProxyFactory();
    }

}
