package com.black.core.spring.factory;

import lombok.extern.log4j.Log4j2;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class DefaultAgentObject implements AgentObject{

    private final Object proxyObject;
    private final Class<?> agentClazz;
    private volatile Object[] args;
    private final Method method;
    private final ThreadLocal<AtomicInteger> indexLocal = new ThreadLocal<>();
    private volatile AgentLayer pointAgentLayer;
    private final ProxyLayerQueue agentLayerQueue;
    private final MethodProxy methodProxy;


    public DefaultAgentObject(Object proxyObject, Class<?> agentClazz, Method method,
                              ProxyLayerQueue agentLayerQueue, MethodProxy methodProxy) {
        this.proxyObject = proxyObject;
        this.agentClazz = agentClazz;
        this.method = method;
        this.agentLayerQueue = agentLayerQueue;
        this.methodProxy = methodProxy;
    }

    @Override
    public Object getProxyObject() {
        return proxyObject;
    }

    @Override
    public Class<?> getAgentClazz() {
        return agentClazz;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public boolean isLastLayer() {
        if (agentLayerQueue.size() == 1){
            return true;
        }
        return indexLocal.get().get() == agentLayerQueue.size() - 2;
    }

    @Override
    public boolean isJDK() {
        return !isCGLIB();
    }

    @Override
    public boolean isCGLIB() {
        return getMethodProxy() != null;
    }

    @Override
    public MethodProxy getMethodProxy() {
        return methodProxy;
    }

    @Override
    public Method getProxyMethod() {
        return method;
    }

    @Override
    public Object doFlow(Object[] args) throws Throwable {
        this.args = args;
        AtomicInteger currenyIndex = indexLocal.get();
        if (currenyIndex == null){
            indexLocal.set(currenyIndex= new AtomicInteger(0));
        }
        currenyIndex.incrementAndGet();
        final int i = currenyIndex.get();
        if (i >= agentLayerQueue.size()){
            if (log.isWarnEnabled()) {
                log.warn("异常下标: {}", i);
            }
            return agentLayerQueue.get(agentLayerQueue.size() - 1).proxy(this);
        }
        pointAgentLayer = agentLayerQueue.get(i);
        return pointAgentLayer.proxy(this);
    }

    @Override
    public AgentLayer getUpperStoryAgentLayer() {
        AtomicInteger currenyIndex = indexLocal.get();
        return currenyIndex.get() == 0 ? null : agentLayerQueue.get(currenyIndex.get() - 1);
    }

    @Override
    public AgentLayer getNextFloorAgentLayer() {
        return indexLocal.get().get() == agentLayerQueue.size() -1 ? null :
                agentLayerQueue.get(indexLocal.get().get() + 1);
    }

    @Override
    public int getNumberOfAgentLayers() {
        return agentLayerQueue.size();
    }

    @Override
    public ProxyLayerQueue getAgentQueue() {
        return agentLayerQueue;
    }

    @Override
    public void clear(Object[] args) {
        AtomicInteger currenyIndex = indexLocal.get();
        if (currenyIndex == null){
            indexLocal.set(currenyIndex = new AtomicInteger(0));
        }
        currenyIndex.set(0);
        pointAgentLayer = null;
        this.args = args;
    }
}
