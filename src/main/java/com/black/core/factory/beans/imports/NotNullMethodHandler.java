package com.black.core.factory.beans.imports;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.MethodWrapper;

/**
 * @author 李桂鹏
 * @create 2023-05-29 16:42
 */
@SuppressWarnings("all")
public class NotNullMethodHandler implements BeanMethodHandler {

    @Override
    public boolean supportReturnProcessor(MethodWrapper method, Object returnValue) {
        return method.hasAnnotation(NotNull.class) && !method.getReturnType().equals(void.class);
    }

    @Override
    public Object processor(MethodWrapper method, Object returnValue, Object bean, BeanFactory factory) {
        if (returnValue == null){
            throw new IllegalStateException("return value should be not null of method: " + method.getName());
        }
        return returnValue;
    }
}
