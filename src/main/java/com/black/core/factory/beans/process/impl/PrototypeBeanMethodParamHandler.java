package com.black.core.factory.beans.process.impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.PrototypeBean;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;

public class PrototypeBeanMethodParamHandler implements BeanMethodHandler {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return parameter.hasAnnotation(PrototypeBean.class);
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            Class<?> type = parameter.getType();
            PrototypeBean annotation = parameter.getAnnotation(PrototypeBean.class);
            previousValue = PrototypeBeanResolverManager.resolvePrototypeType(type, annotation.createBatch(), factory, parameter);
        }
        return BeanMethodHandler.super.handler(method, parameter, bean, factory, previousValue);
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            Class<?> type = pw.getType();
            PrototypeBean annotation = pw.getAnnotation(PrototypeBean.class);
            previousValue = PrototypeBeanResolverManager.resolvePrototypeType(type, annotation.createBatch(), factory, pw);
        }
        return BeanMethodHandler.super.structure(cw, pw, factory, previousValue);
    }
}
