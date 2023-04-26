package com.black.rpc;

import com.black.rpc.annotation.Actuator;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcMethodRegister {

    private final RpcConfiguration configuration;

    private final Map<String, MethodInvoker> methodCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();

    public RpcMethodRegister(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    public MethodInvoker getMethodInvoker(String methodName){
        return methodCache.get(methodName);
    }

    public Map<Class<?>, Object> getProxyCache() {
        return proxyCache;
    }

    public Map<String, MethodInvoker> getMethodCache() {
        return methodCache;
    }

    public RpcConfiguration getConfiguration() {
        return configuration;
    }

    public int methodSize(){
        return methodCache.size();
    }

    public void registerMethodInvoker(MethodWrapper mw, ClassWrapper<?> cw){
        BeanFactory beanFactory = configuration.getBeanFactory();
        Object singleBean = beanFactory.getSingleBean(cw.getPrimordialClass());
        registerMethodInvoker(mw, singleBean);
    }

    public void registerMethodInvoker(MethodWrapper mw, Object bean){
        Class<?> primordialClass = BeanUtil.getPrimordialClass(bean);
        ClassWrapper<?> cw = ClassWrapper.get(primordialClass);
        String methodName = parseMethod(mw, cw);
        registerMethodInvoker(mw, bean, methodName);
    }

    public void registerMethodInvoker(MethodWrapper mw, Object bean, String methodName){
        if (methodCache.containsKey(methodName)) {
            throw new IllegalStateException("an actuator with the same name cannot exist, name:" + methodName);
        }
        MethodInvoker invoker = new MethodInvoker(mw, bean, methodName);
        methodCache.put(methodName, invoker);
    }

    private String parseMethod(MethodWrapper mw, ClassWrapper<?> cw){

        Actuator actuator = AnnotatedElementUtils.findMergedAnnotation(mw.getMethod(), Actuator.class);
        if (actuator == null){
            actuator = AnnotatedElementUtils.findMergedAnnotation(cw.get(), Actuator.class);
        }
        String methodName = "".equals(actuator.value()) ? mw.getName() : actuator.value();
        return methodName;
    }
}
