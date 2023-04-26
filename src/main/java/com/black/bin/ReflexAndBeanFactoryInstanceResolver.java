package com.black.bin;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;

import java.util.Map;

public class ReflexAndBeanFactoryInstanceResolver implements InstanceBeanResolver{
    @Override
    public boolean support(InstanceType type) {
        return type == InstanceType.REFLEX_AND_BEAN_FACTORY;
    }

    @Override
    public <T> T instance(Class<T> type, Map<String, Object> source) {
        FactoryManager.init();
        BeanFactory beanFactory = FactoryManager.getBeanFactory();
        try {
            type.getConstructor();
            return ReflexUtils.instance(type);
        } catch (NoSuchMethodException e) {
            return beanFactory.prototypeCreateBean(type, source);
        }
    }
}
