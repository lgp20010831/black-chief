package com.black.utils;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;

import java.lang.reflect.Method;

/**
 * @author shkstart
 * @create 2023-04-13 16:13
 */
public class Java implements ApplyProxyLayer {

    private final Object object;


    public Java(Object object) {
        if (object == null){
            this.object = null;
        }else {
            this.object = ApplyProxyFactory.proxy(object, this);
        }

    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        return doProxy(args, method, beanClass, template);
    }

    protected Object doProxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        return template.invokeOriginal(args);
    }

    public Java val(){
        return this;
    }

    public String text(){
        return object.toString();
    }

    public Java copy(){
        return this;
    }



}
