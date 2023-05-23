package com.black.core.factory.beans.agent;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-05-23 16:42
 */
@SuppressWarnings("all")
public abstract class AbstractBeansProxy {


    protected final BeanFactory factory;

    protected final BeanDefinitional<?> definitional;

    protected AbstractBeansProxy(BeanFactory factory, BeanDefinitional<?> definitional) {
        this.factory = factory;
        this.definitional = definitional;
    }

    protected boolean isQualified(MethodWrapper methodWrapper){
        return definitional.isQualified(methodWrapper);
    }

    protected Object[] checkArgs(Object[] args){
        return args == null ? new Object[0] : args;
    }

    protected void checkNotNullArgs(MethodWrapper methodWrapper, Object[] args){
        ParameterWrapper[] parameterArray = methodWrapper.getParameterArray();
        for (ParameterWrapper parameterWrapper : parameterArray) {
            Object arg = args[parameterWrapper.getIndex()];
            if (parameterWrapper.hasAnnotation(NotNull.class)){
                if (arg == null){
                    throw new IllegalArgumentException("parameter should not be empty: " + parameterWrapper.getName());
                }
            }
        }
    }

    protected Object[] prepareArgs(MethodWrapper methodWrapper, Object[] args, Object bean){
        return factory.prepareMethodParams(args, bean, methodWrapper);
    }

    protected Object resolveResult(MethodWrapper methodWrapper, Object result, Object bean){
        return factory.afterInvokeMethod(bean, result, methodWrapper);
    }
}
