package com.black.bin;

import java.lang.reflect.Method;

public class ReleasableBeanLayer implements ApplyProxyLayer{
    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        Object result = template.invokeOriginal(args);
        if (isReleasableMethod(method)){
            ObjectRecycleBin.release(template.getProxyBean());
        }
        return result;
    }

    private boolean isReleasableMethod(Method method){
        return method.getName().equals("release") &&
                method.getParameterCount() == 0 &&
                method.getReturnType() == void.class;
    }
}
