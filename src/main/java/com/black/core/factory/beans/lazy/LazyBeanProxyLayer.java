package com.black.core.factory.beans.lazy;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.utils.ProxyUtil;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-05-23 11:22
 */
@SuppressWarnings("all")
public class LazyBeanProxyLayer implements ApplyProxyLayer {

    @Override
    public boolean filterObjectMethod() {
        return false;
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        if (ProxyUtil.isObjectMethod(method)){
            return template.invokeSupper(args);
        }
        return template.invokeOriginal(args);
    }
}
