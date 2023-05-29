package com.black.core.factory.beans.agent;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.util.ApplicationUtil;

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
            tryLock(mw, args);
            try {
                Object result;
                if (isNeedReckon(mw)){
                    Object[] finalArgs = args;
                    result = ApplicationUtil.programRunMills(() -> {
                        try {
                            return template.invokeOriginal(finalArgs);
                        } catch (Throwable e) {
                            throw new IllegalStateException("An exception occurred during " +
                                    "the timing process", e);
                        }
                    }, "[BEAN PROXY]");
                }else {
                    result = template.invokeOriginal(args);
                }
                return resolveResult(mw, result, bean);
            }finally {
                tryUnlock();
            }
        }
    }
}
