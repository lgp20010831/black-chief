package com.black.core.factory.beans.config_collect520;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;

@SuppressWarnings("all")
public class NestPropertyParamHandler extends AbstractNestPropertyHandler implements BeanMethodHandler {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return parameter.hasAnnotation(NestProperty.class);
    }


    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            previousValue = factory.getSingleBean(parameter.getType());
        }
        handlerTarget(previousValue, factory);
        return previousValue;
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            previousValue = factory.getSingleBean(pw.getType());
        }
        handlerTarget(previousValue, factory);
        return previousValue;
    }
}
