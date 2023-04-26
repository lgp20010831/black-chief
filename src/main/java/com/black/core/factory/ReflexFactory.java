package com.black.core.factory;

import com.black.core.spring.instance.InstanceWrapper;

import java.util.Map;
@SuppressWarnings("all")
//反射工厂, 规定了通过 class 来创建
public interface ReflexFactory<P> extends CommonFactory<InstanceWrapper<?>, P> {

    //提供一个创建, Wrapper 的接口
    InstanceWrapper<?> createWrapper(Class<?> beanClass);

    InstanceWrapper<?> createWrapper(Class<?> beanClass, Map<String, Object> contructMapArgs);

    //定义一个 wrapper factory
    InstanceWrapperFactory getWrapperFactory();
}
