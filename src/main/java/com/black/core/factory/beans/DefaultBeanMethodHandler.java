package com.black.core.factory.beans;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.annotation.SingleBean;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;

public class DefaultBeanMethodHandler implements BeanMethodHandler {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return (parameter.getAnnotations().isEmpty() || parameter.hasAnnotation(SingleBean.class))
                && !ClassWrapper.isBasic(parameter.getType().getName());
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            try {

                return factory.getSingleBean(parameter.getType());
            }catch (BeanFactorysException ex){
                throw new BeanFactorysException("An exception occurred while trying to " +
                        "create a parameter from the factory while parsing an object method " +
                        "parameter, Type of parameter to create: [" + parameter.getType() + "], " +
                        "object of delegation: [" + bean + "], analytical method: [" + method.getName() + "]" , ex);
            }
        }
        return previousValue;
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        if (previousValue == null){
            try {

                return factory.getSingleBean(pw.getType());
            }catch (BeanFactorysException ex){
                throw new BeanFactorysException("An exception occurred while trying to " +
                        "create a parameter from the factory while parsing an object method " +
                        "parameter, Type of parameter to create: [" + pw.getType() + "], " +
                        "analytical constructor: [" + cw.getConstructor()+ "]" , ex);
            }
        }
        return previousValue;
    }
}
