package com.black.bin;

import com.black.utils.ProxyUtil;
import lombok.Getter;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class ProxyTemplate {

    final Object proxyBean;

    final Object bean;

    final MethodProxy methodProxy;

    final Method method;

    final Class<?> beanClass;

    final LinkedBlockingQueue<ApplyProxyLayer> proxyLayers;

    public ProxyTemplate(Object proxyBean, Object bean, MethodProxy methodProxy, Method method, Class<?> beanClass, LinkedBlockingQueue<ApplyProxyLayer> proxyLayers) {
        this.proxyBean = proxyBean;
        this.bean = bean;
        this.methodProxy = methodProxy;
        this.method = method;
        this.beanClass = beanClass;
        this.proxyLayers = proxyLayers;
    }

    public MethodProxy getMethodProxy() {
        return methodProxy;
    }

    public Object getBean() {
        return bean;
    }

    public Object getProxyBean() {
        return proxyBean;
    }

    public Class<?> getBeanClass(){
        return beanClass;
    }

    public boolean isJDK(){
        return getMethodProxy() == null;
    }

    public Object invokeSupper(Object[] args) throws Throwable {
        if (isJDK()){
            throw new IllegalStateException("current is jdk proxy");
        }

        MethodProxy methodProxy = getMethodProxy();
        return methodProxy.invokeSuper(proxyBean, args);
    }

    public Object invokeOriginal(Object[] args) throws Throwable {
        if (ProxyUtil.isObjectMethod(method)) {
            return invokeOriginal0(args);
        }
        ApplyProxyLayer next = proxyLayers.poll();
        if (next != null){
            return next.proxy(args, method, beanClass, this);
        }else {
            return invokeOriginal0(args);
        }
    }

    public Object invokeOriginal0(Object[] args) throws Throwable{
        if (!method.isAccessible()){
            method.setAccessible(true);
        }
        return method.invoke(bean, args);
    }

}
