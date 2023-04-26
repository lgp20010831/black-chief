package com.black.core.spring.instance;

public class DefaultInstanceElementFactory implements InstanceElementFactory{
    @Override
    public <T> InstanceElement<T> createElement(Class<T> beanClass) {
        return new InstanceWrapper<T>(beanClass, this);
    }
}
