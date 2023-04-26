package com.black.core.factory.beans.process.inter;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;

public interface BeanMethodHandler extends BeanFactoryProcessor {


    default boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean){
        return false;
    }

    default Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue){
        return previousValue;
    }

    default Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue){
        return previousValue;
    }

    default boolean supportReturnProcessor(MethodWrapper method, Object returnValue){
        return false;
    }

    default Object processor(MethodWrapper method, Object returnValue, Object bean, BeanFactory factory){
        return returnValue;
    }
}
