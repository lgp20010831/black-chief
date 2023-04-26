package com.black.core.factory;

import com.black.core.spring.instance.InstanceElement;

import java.util.Map;

public interface InstanceWrapperFactory extends CommonFactory<Class<?>, InstanceElement<?>> {

    InstanceElement<?> get(Class<?> beanClass, Map<String, Object> contructMapArgs);
}
