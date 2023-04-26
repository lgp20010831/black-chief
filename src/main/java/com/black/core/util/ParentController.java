package com.black.core.util;

import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParentController implements BeanFactoryAware {

    protected BeanFactory factory;
    protected final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    protected <M> M getBean(@NonNull Class<M> beanType){
        Assert.notNull(factory, "factory 异常为空");
        if (cache.containsKey(beanType)){
            return (M) cache.get(beanType);
        }
        try {
            M bean = factory.getBean(beanType);
            cache.put(beanType, bean);
            return bean;
        }catch (BeansException be){
            throw new IllegalStateException("无法获取 bean 对象: " + beanType.getSimpleName());
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }

    public BeanFactory getFactory() {
        return factory;
    }
}
