package com.black.core.factory.beans.lazy;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.annotation.Key;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.MethodWrapper;

import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-23 18:00
 */
@SuppressWarnings("all")
public class DefaultBeanMethodReturnValueHandler implements BeanMethodHandler {

    @Override
    public boolean supportReturnProcessor(MethodWrapper method, Object returnValue) {
        return !method.getReturnType().equals(void.class);
    }

    @Override
    public Object processor(MethodWrapper method, Object returnValue, Object bean, BeanFactory factory) {
        if (returnValue == null || !(returnValue instanceof Map)){
            return returnValue;
        }
        if (!method.hasAnnotation(Key.class)){
            return returnValue;
        }

        Map<String, Object> source = (Map<String, Object>) returnValue;
        Map<String, Object> handlerSource = KeyUtils.handlerKey(method.get(), source.values());
        source.clear();
        source.putAll(handlerSource);
        return source;
    }
}
