package com.black.resolve.param;

import com.black.resolve.ResolveException;
import com.black.resolve.inter.ParameterHandler;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.utils.ReflectionUtils;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractParamHandler implements ParameterHandler {

    protected Class<?> getGenericByCollection(ParameterWrapper pw){
        Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
        if (genericVals.length == 0){
            return Object.class;
        }
        if (genericVals.length == 1){
            return genericVals[0];
        }
        throw new ResolveException("collection generics are not unique");
    }

    protected boolean isCollection(ParameterWrapper pw){
        return Collection.class.isAssignableFrom(pw.getType());
    }

    protected boolean isMap(ParameterWrapper pw){
        return Map.class.isAssignableFrom(pw.getType());
    }

    protected Class<?>[] getGenericByMap(ParameterWrapper pw){
        Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
        if (genericVals.length == 0){
            return new Class[]{String.class, Object.class};
        }
        if (genericVals.length == 2){
            return genericVals;
        }
        throw new ResolveException("collection generics are not 2");
    }
}
