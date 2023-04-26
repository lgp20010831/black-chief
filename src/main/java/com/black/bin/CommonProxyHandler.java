package com.black.bin;

import com.black.core.tools.BeanUtil;
import com.black.utils.ProxyUtil;
import lombok.NonNull;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;

public class CommonProxyHandler implements InvocationHandler, MethodInterceptor {

    //实际对象
    final Object bean;

    final LinkedBlockingQueue<ApplyProxyLayer> applyProxyLayers = new LinkedBlockingQueue<>();

    final ApplyProxyLayer applyProxyLayer;

    final Class<?> type;

    public CommonProxyHandler(@NonNull Object bean, @NonNull ApplyProxyLayer applyProxyLayer) {
        this.bean = bean;
        this.applyProxyLayer = applyProxyLayer;
        type = BeanUtil.getPrimordialClass(bean);
        applyProxyLayers.add(applyProxyLayer);
    }

    public void addLayer(ApplyProxyLayer proxyLayer){
        applyProxyLayers.add(proxyLayer);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (applyProxyLayer.filterObjectMethod() && ProxyUtil.isObjectMethod(method)) {
            return method.invoke(bean, args);
        }

        if (ProxyUtil.isDefaultMethod(method)){
            return ProxyUtil.invokeDefaultMethod(method, bean, args);
        }

        if (isApplyProxyMethod(method)){
            return invokeApplyProxyMethod(method, args, true);
        }

        LinkedBlockingQueue<ApplyProxyLayer> copyQueue = new LinkedBlockingQueue<>(this.applyProxyLayers);
        ApplyProxyLayer proxyLayer = copyQueue.poll();
        if (proxyLayer == null){
            return null;
        }else {
            return proxyLayer.proxy(args, method, type, new ProxyTemplate(
                    proxy, bean, null, method,
                    type, copyQueue));
        }
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (applyProxyLayer.filterObjectMethod() && ProxyUtil.isObjectMethod(method)) {
            return method.invoke(bean, objects);
        }

        if (isApplyProxyMethod(method)){
            return invokeApplyProxyMethod(method, objects, false);
        }

        LinkedBlockingQueue<ApplyProxyLayer> copyQueue = new LinkedBlockingQueue<>(this.applyProxyLayers);
        ApplyProxyLayer proxyLayer = copyQueue.poll();
        if (proxyLayer == null){
            return methodProxy.invokeSuper(o, objects);
        }else {
            return proxyLayer.proxy(objects, method, type, new ProxyTemplate(
                    o, bean, methodProxy, method,
                    type, copyQueue));
        }

    }

    private boolean isApplyProxyMethod(Method method){
        return method.getDeclaringClass().equals(ApplyProxy.class);
    }

    private Object invokeApplyProxyMethod(Method method, Object[] objects, boolean jdk){
        if (method.getName().equals("getThis") && method.getParameterCount() == 0){
            return bean;
        }

        if (method.getName().equals("getType") && method.getParameterCount() == 0){
            return type;
        }

        if (method.getName().equals("isJdk") && method.getParameterCount() == 0){
            return jdk;
        }
        return null;
    }
}
