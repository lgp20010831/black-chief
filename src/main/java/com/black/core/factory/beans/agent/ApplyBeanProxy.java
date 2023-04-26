package com.black.core.factory.beans.agent;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;

public class ApplyBeanProxy implements ApplyProxyLayer {

    private final BeanFactory factory;

    private final BeanDefinitional<?> definitional;

    public ApplyBeanProxy(BeanFactory factory, BeanDefinitional<?> definitional) {
        this.factory = factory;
        this.definitional = definitional;
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        MethodWrapper mw = MethodWrapper.get(method);
        boolean proxyMethod = definitional.isQualified(mw);
        Object bean = template.getBean();
        if (proxyMethod) {
            args = factory.prepareMethodParams(args, bean, mw);
        }
        Object result = template.invokeOriginal(args);
        if (proxyMethod){
            result = factory.afterInvokeMethod(bean, result, mw);
        }
        return result;
    }
}
