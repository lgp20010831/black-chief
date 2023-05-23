package com.black.core.factory.beans.agent;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;

public class ApplyBeanProxy extends AbstractBeansProxy implements ApplyProxyLayer {


    public ApplyBeanProxy(BeanFactory factory, BeanDefinitional<?> definitional) {
        super(factory, definitional);

    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        MethodWrapper mw = MethodWrapper.get(method);
        boolean proxyMethod = isQualified(mw);
        if (!proxyMethod){
            return template.invokeOriginal(args);
        }else {
            Object bean = template.getBean();
            args = checkArgs(args);
            checkNotNullArgs(mw, args);
            args = prepareArgs(mw, args, bean);
            Object result = template.invokeOriginal(args);
            return resolveResult(mw, result, bean);
        }
    }
}
