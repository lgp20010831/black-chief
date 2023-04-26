package com.black.bin;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.instance.InstanceFactory;

import java.util.Map;

public class InstanceResolver implements InstanceBeanResolver{
    @Override
    public boolean support(InstanceType type) {
        return type == InstanceType.INSTANCE;
    }

    @Override
    public <T> T instance(Class<T> type, Map<String, Object> source) {
        InstanceFactory instanceFactory = FactoryManager.initAndGetInstanceFactory();
        return instanceFactory.getInstance(type);
    }
}
