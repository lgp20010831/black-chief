package com.black.mvc;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ApplyProxyMethodInvocation;
import com.black.bin.ProxyTemplate;
import com.black.core.aop.code.GlobalAdviceMethodIntercept;
import com.black.core.aop.listener.EnableAopStartListener;
import com.black.utils.ProxyUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;

public class SupportAopProxyLayer implements ApplyProxyLayer {

    private final Class<?> target;

    public SupportAopProxyLayer(Class<?> target) {
        this.target = target;
    }

    @Override
    public boolean filterObjectMethod() {
        return false;
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {

        if (ProxyUtil.isObjectMethod(method)) {
            return template.invokeOriginal(args);
        }

        Object bean = template.getBean();
        if (EnableAopStartListener.isOpen()){
            if (AopUtils.isAopProxy(bean)) {
                return template.invokeOriginal(args);
            }
        }
        MethodInvocation invocation = ApplyProxyMethodInvocation.wrapper(template, args);
        GlobalAdviceMethodIntercept intercept = GlobalAdviceMethodIntercept.getInstance();
        return intercept.invoke(invocation);
    }
}
