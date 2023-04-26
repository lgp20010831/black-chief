package com.black.core.spring.factory;

import com.black.core.aop.code.HijackObject;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class HijackAgentObject implements AgentObject{

    final HijackObject hijackObject;

    public static HijackAgentObject of(HijackObject object){
        return new HijackAgentObject(object);
    }

    public HijackAgentObject(HijackObject hijackObject) {
        this.hijackObject = hijackObject;
    }

    @Override
    public Object getProxyObject() {
        return hijackObject.getInvocation().getThis();
    }

    @Override
    public Class<?> getAgentClazz() {
        return hijackObject.getClazz();
    }

    @Override
    public Object[] getArgs() {
        return hijackObject.getArgs();
    }

    @Override
    public boolean isLastLayer() {
        throw new IllegalStateException("Hijack 不支持提供该信息");
    }

    @Override
    public boolean isJDK() {
        return hijackObject.getClazz().isInterface();
    }

    @Override
    public boolean isCGLIB() {
        return !hijackObject.getClazz().isInterface();
    }

    @Override
    public MethodProxy getMethodProxy() {
        return null;
    }

    @Override
    public Method getProxyMethod() {
        return hijackObject.getMethod();
    }

    @Override
    public Object doFlow(Object[] args) throws Throwable {
        return hijackObject.doRelease(args);
    }

    @Override
    public AgentLayer getUpperStoryAgentLayer() {
        throw new IllegalStateException("Hijack 不支持提供该信息");
    }

    @Override
    public AgentLayer getNextFloorAgentLayer() {
        throw new IllegalStateException("Hijack 不支持提供该信息");
    }

    @Override
    public int getNumberOfAgentLayers() {
        throw new IllegalStateException("Hijack 不支持提供该信息");
    }

    @Override
    public ProxyLayerQueue getAgentQueue() {
        throw new IllegalStateException("Hijack 不支持提供该信息");
    }

    @Override
    public void clear(Object[] args) {
        throw new IllegalStateException("Hijack 不支持 clear 操作");
    }
}
