package com.black.bin;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;

import java.util.Map;

public class BeanFactorySingleInstanceResolver implements InstanceBeanResolver{
    @Override
    public boolean support(InstanceType type) {
        return type == InstanceType.BEAN_FACTORY_SINGLE;
    }

    @Override
    public <T> T instance(Class<T> type, Map<String, Object> source) {
        FactoryManager.init();
        BeanFactory beanFactory = FactoryManager.getBeanFactory();
        return beanFactory.getSingleBean(type, source);
    }
}
