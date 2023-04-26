package com.black.core.factory;

import com.black.core.spring.instance.DefaultInstanceElementFactory;
import com.black.core.spring.instance.InstanceElement;
import com.black.core.spring.instance.InstanceElementFactory;

import java.util.Map;
import java.util.UUID;

public class DefaultInstanceWrapperFactory implements InstanceWrapperFactory {

    private final InstanceElementFactory instanceElementFactory;

    public DefaultInstanceWrapperFactory() {
        instanceElementFactory = new DefaultInstanceElementFactory();
    }

    @Override
    public InstanceElement<?> get(Class<?> beanClass, Map<String, Object> contructMapArgs) {
        return instanceElementFactory.createElement(beanClass);
    }

    @Override
    public InstanceElement<?> get(Class<?> p) {
        return get(p, null);
    }

    @Override
    public String id() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Factory getParent() {
        return null;
    }
}
