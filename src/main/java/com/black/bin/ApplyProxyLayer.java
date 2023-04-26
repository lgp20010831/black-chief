package com.black.bin;

import java.lang.reflect.Method;

public interface ApplyProxyLayer {


    default boolean filterObjectMethod(){
        return true;
    }

    Object proxy(Object[] args,
                 Method method,
                 Class<?> beanClass,
                 ProxyTemplate template) throws Throwable;

}
