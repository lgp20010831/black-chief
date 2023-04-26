package com.black.core.spring.factory;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.ConstructorWrapper;
import com.black.core.spring.instance.InstanceElement;
import com.black.core.spring.instance.InstanceElementFactory;
import com.black.core.spring.instance.InstanceFactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultProxyFactory implements ReusingProxyFactory {

    private final Map<Class<?>, AbstractProxyHandler> proxyHandlerMap = new HashMap();

    private final Map<Class<?>, Object> proxyCache = new HashMap<>();

    private final Map<AbstractProxyHandler, ProxyLayerQueue> proxyLayerQueueMap = new HashMap<>();

    private final AgentObjectBuilderFactory agentObjectBuilderFactory;

    private final CGlibAndJDKProxyFactory cGlibAndJDKProxyFactory;

    public DefaultProxyFactory() {
        agentObjectBuilderFactory = obtainAgentObjectBuilderFactory();
        cGlibAndJDKProxyFactory = obtainCGlibAndJDKProxyFactory();
    }

    @Override
    public boolean isAgent(Class<?> proxiedObject) {
        return proxyHandlerMap.containsKey(proxiedObject);
    }

    @Override
    public <T> T getAlreadyProxiedObject(Class<T> type) {
        return (T) proxyCache.get(type);
    }

    protected CGlibAndJDKProxyFactory obtainCGlibAndJDKProxyFactory(){
        return new DefaultCGlibAndJDKProxyFactory();
    }

    protected AgentObjectBuilderFactory obtainAgentObjectBuilderFactory(){
        return new DefaultAgentObjectBuilderFactory();
    }

    protected AbstractProxyHandler obtainAbstractProxyHandler(){
        return new DefaultProxyHandler(agentObjectBuilderFactory);
    }

    protected ProxyLayerQueue obtainProxyLayerQueue(AbstractProxyHandler abstractProxyHandler){
        return new ProxyLayerQueue(abstractProxyHandler);
    }

    @Override
    public <T> T prototypeProxy(Class<? extends T> proxiedObject, AgentLayer layer, Class<?>[] paramters, Object[] args) {
        AbstractProxyHandler proxyHandler = obtainAbstractProxyHandler();
        ProxyLayerQueue layerQueue = obtainProxyLayerQueue(proxyHandler);
        proxyHandler.setLayerQueue(layerQueue);
        T proxy = doProxy(proxiedObject, paramters, args, proxyHandler);
        if (layer != null){
            layerQueue.add(layer);
        }
        return proxy;
    }


    @Override
    public <T> T proxy(Class<? extends T> proxiedObject, Class<?>[] paramters, Object[] args, AgentLayer agentLayer, int index) {
        AbstractProxyHandler abstractProxyHandler = proxyHandlerMap.computeIfAbsent(proxiedObject, pro -> obtainAbstractProxyHandler());
        ProxyLayerQueue proxyLayerQueue= proxyLayerQueueMap.computeIfAbsent(abstractProxyHandler, this::obtainProxyLayerQueue);
        abstractProxyHandler.setLayerQueue(proxyLayerQueue);
        Object proxy;
        if ((proxy = proxyCache.get(proxiedObject)) == null){
            proxy = doProxy(proxiedObject, paramters, args, abstractProxyHandler);
            proxyCache.put(proxiedObject, proxy);
        }
        if (agentLayer != null){
            proxyLayerQueue.add(index, agentLayer);
        }
        return (T) proxy;
    }

    @Override
    public <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, InstanceFactory instanceFactory, int index) {
        AbstractProxyHandler abstractProxyHandler = proxyHandlerMap.computeIfAbsent(proxiedObject, pro -> obtainAbstractProxyHandler());
        ProxyLayerQueue proxyLayerQueue= proxyLayerQueueMap.computeIfAbsent(abstractProxyHandler, this::obtainProxyLayerQueue);
        abstractProxyHandler.setLayerQueue(proxyLayerQueue);
        Object proxy;
        if ((proxy = proxyCache.get(proxiedObject)) == null){
            proxy = doProxy0(proxiedObject, instanceFactory, abstractProxyHandler);
            proxyCache.put(proxiedObject, proxy);
        }
        if (layer != null){
            proxyLayerQueue.add(index, layer);
        }
        return (T) proxy;
    }

    @Override
    public <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, BeanFactory beanFactory, int index) {
        AbstractProxyHandler abstractProxyHandler = proxyHandlerMap.computeIfAbsent(proxiedObject, pro -> obtainAbstractProxyHandler());
        ProxyLayerQueue proxyLayerQueue= proxyLayerQueueMap.computeIfAbsent(abstractProxyHandler, this::obtainProxyLayerQueue);
        abstractProxyHandler.setLayerQueue(proxyLayerQueue);
        Object proxy;
        if ((proxy = proxyCache.get(proxiedObject)) == null){
            proxy = doProxy0(proxiedObject, beanFactory, abstractProxyHandler);
            proxyCache.put(proxiedObject, proxy);
        }
        if (layer != null){
            proxyLayerQueue.add(index, layer);
        }
        return (T) proxy;
    }


    protected <T> T doProxy0(Class<T> proxiedObject, BeanFactory beanFactory, AbstractProxyHandler abstractProxyHandler){
        if (proxiedObject.isInterface()){
            return doProxy(proxiedObject, null, null, abstractProxyHandler);
        }
        BeanDefinitional<T> definitional = beanFactory.createDefinitional(proxiedObject, false);
        ConstructorWrapper<?> mainConstructor;
        try {
             mainConstructor = definitional.mainConstructor();
        } catch (NoSuchMethodException e) {
            throw new ProxyInvokeException(e);
        }
        Class<?>[] parameterTypes = mainConstructor.getParameterTypes();
        Map<ParameterWrapper, BeanDefinitional<?>> pbdm = definitional.instanceConstructorWrapper(mainConstructor, beanFactory);
        Object[] args = beanFactory.getConstructorArgs(pbdm, null, mainConstructor);
        T proxy = doProxy(proxiedObject, parameterTypes, args, abstractProxyHandler);
        beanFactory.registerBean(proxy);
        return proxy;
    }

    protected <T> T doProxy0(Class<T> proxiedObject, InstanceFactory instanceFactory, AbstractProxyHandler abstractProxyHandler){
        if (proxiedObject.isInterface()){
            return doProxy(proxiedObject, null, null, abstractProxyHandler);
        }
        InstanceElementFactory elementFactory = instanceFactory.getElementFactory();
        InstanceElement<? extends T> proxyElement = elementFactory.createElement(proxiedObject);
        Constructor<? extends T> constructor = proxyElement.instanceConstructor();
        if (constructor == null){
            throw new RuntimeException("找不到合适的构造器进行代理创建 -- " + proxiedObject);
        }
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = instanceFactory.getInstance(parameterTypes[i]);
        }
        T proxy = doProxy(proxiedObject, parameterTypes, args, abstractProxyHandler);
        instanceFactory.registerInstance(proxiedObject, proxy);
        return proxy;
    }

    @Override
    public <T> T proxy(Class<? extends T> proxiedObject, AgentLayer layer, InstanceFactory instanceFactory) {
        return proxy(proxiedObject, layer, instanceFactory, -1);
    }



    @Override
    public int getNumberOfAgentLayers(Class<?> proxiedObject) {
        if (proxyHandlerMap.containsKey(proxiedObject)){
            AbstractProxyHandler abstractProxyHandler = proxyHandlerMap.get(proxiedObject);
            if (proxyLayerQueueMap.containsKey(abstractProxyHandler)){
                return proxyLayerQueueMap.get(abstractProxyHandler).size() -1;
            }
        }
        return 0;
    }

    @Override
    public ProxyLayerQueue getAgentQueue(Class<?> proxiedObject) {
        if (proxyHandlerMap.containsKey(proxiedObject)){
            AbstractProxyHandler abstractProxyHandler = proxyHandlerMap.get(proxiedObject);
           if (abstractProxyHandler != null){
               return proxyLayerQueueMap.get(abstractProxyHandler);
           }
        }
        return null;
    }

    protected <T> T doProxy(Class<? extends T> proxiedObject, Class<?>[] paramters, Object[] args, AbstractProxyHandler abstractProxyHandler){
        if (proxiedObject.isEnum()){
            throw new RuntimeException("无法代理枚举类");
        }
        if (proxiedObject.isInterface()){
            return cGlibAndJDKProxyFactory.proxyJDK(proxiedObject, abstractProxyHandler);
        }else {
            return cGlibAndJDKProxyFactory.proxyCGlib(proxiedObject, abstractProxyHandler, paramters, args);
        }
    }

    @Override
    public Collection<Object> getProxyInstanceCache() {
        return proxyCache.values();
    }
}
