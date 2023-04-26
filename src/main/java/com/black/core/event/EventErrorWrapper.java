package com.black.core.event;

import com.black.core.tools.BeanUtil;
import lombok.Getter;


import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Getter
public class EventErrorWrapper {


    private final Method method;
    private final Object obj;
    private final boolean asyn;
    private final Class<?> primordialClass;
    ResolverThrowable resolverThrowable;
    public EventErrorWrapper(Method method, Object obj, ResolverThrowable resolverThrowable) {
        this.method = method;
        this.obj = obj;
        primordialClass = BeanUtil.getPrimordialClass(obj);
        this.resolverThrowable = resolverThrowable;
        asyn = resolverThrowable.asyn();
    }

    public void invoke(Throwable e, Object event, String entry){
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[3];
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if (Throwable.class.isAssignableFrom(type)){
                args[i] = e;
            }else if (String.class.equals(type)){
                args[i] = entry;
            }else if (Object.class.equals(type)){
                args[i] = event;
            }
        }
        try {
            method.invoke(obj, args);
        }catch (Throwable ex){
            throw new RuntimeException(ex);
        }
    }
}
