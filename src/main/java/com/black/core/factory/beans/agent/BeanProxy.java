package com.black.core.factory.beans.agent;

import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;

@Log4j2
public class BeanProxy extends AbstractBeansProxy implements AgentLayer {


    public BeanProxy(BeanFactory factory, BeanDefinitional<?> definitional) {
        super(factory, definitional);

    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Method proxyMethod = layer.getProxyMethod();
        MethodWrapper mw = MethodWrapper.get(proxyMethod);
        Object proxyObject = layer.getProxyObject();
        Object[] args = layer.getArgs();
        boolean qualified = isQualified(mw);
        if (!qualified){
            return layer.doFlow(args);
        }else {
            args = checkArgs(args);
            checkNotNullArgs(mw, args);
            args = prepareArgs(mw, args, proxyObject);
            try {
                Object result = layer.doFlow(args);
                return resolveResult(mw, result, proxyObject);
            }finally {
                tryUnlock();
            }
        }
    }
}
