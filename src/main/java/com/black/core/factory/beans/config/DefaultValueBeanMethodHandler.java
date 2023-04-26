package com.black.core.factory.beans.config;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.DefaultValue;

public class DefaultValueBeanMethodHandler implements BeanMethodHandler {
    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return parameter.hasAnnotation(DefaultValue.class);
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            previousValue = doResolver(parameter, factory);
        }
        return previousValue;
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        if (previousValue != null){
            previousValue = doResolver(pw, factory);
        }
        return previousValue;
    }


    Object doResolver(ParameterWrapper parameter, BeanFactory factory){
        DefaultValue annotation = parameter.getAnnotation(DefaultValue.class);
        String value = annotation.value();
        Object val = value;
        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        if (typeHandler != null){
            val = typeHandler.convert(parameter.getType(), val);
        }
        return val;
    }
}
