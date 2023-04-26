package com.black.core.factory.beans.agent;

import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;

@Log4j2
public class BeanProxy implements AgentLayer {

    private final BeanFactory factory;

    private final BeanDefinitional<?> definitional;

    public BeanProxy(BeanFactory factory, BeanDefinitional<?> definitional) {
        this.factory = factory;
        this.definitional = definitional;
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Method proxyMethod = layer.getProxyMethod();
        MethodWrapper mw = MethodWrapper.get(proxyMethod);
        Object proxyObject = layer.getProxyObject();
        Object[] args = layer.getArgs();
        boolean qualified = definitional.isQualified(mw);
        if (qualified) {
            if (log.isInfoEnabled()) {
                log.info("factory: {}, pretreatment args from method: {}", factory.getClass().getSimpleName(), mw.getName());
            }
            args = factory.prepareMethodParams(args, proxyObject, mw);
        }
        Object result = layer.doFlow(args);
        if (qualified){
            result = factory.afterInvokeMethod(proxyObject, result, mw);
        }
        return result;
    }
}
