package com.black.aop;

import com.black.aop.impl.CommonInterceptConditionHandler;
import com.black.aop.impl.InterceptOnAnnotationHandler;
import com.black.aop.impl.ResolveIntercetConditionHandler;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.annotation.Sort;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.mvc.SpringBeanRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 李桂鹏
 * @create 2023-06-16 13:45
 */
@SuppressWarnings("all")
public class AopLoadRegister {

    private static AopLoadRegister register;

    private static final LinkedBlockingQueue<ResolveIntercetConditionHandler> handlers = new LinkedBlockingQueue<>();

    static {
        handlers.add(new CommonInterceptConditionHandler());
        handlers.add(new InterceptOnAnnotationHandler());
    }

    public static LinkedBlockingQueue<ResolveIntercetConditionHandler> getHandlers() {
        return handlers;
    }

    public synchronized static AopLoadRegister getInstance() {
        if (register == null){
            register = new AopLoadRegister();
        }
        return register;
    }

    private AopLoadRegister(){}

    public Object load(Class<?> type){
        Object instance = InstanceBeanManager.instance(type, InstanceType.BEAN_FACTORY_SINGLE);
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(type);
        Collection<MethodWrapper> methods = classWrapper.getMethods();
        List<AopMethodWrapper> aopMethodWrappers = new ArrayList<>();
        for (MethodWrapper method : methods) {
            AopMethodWrapper aopMethodWrapper = loadMethod(method, instance);
            if (aopMethodWrapper != null){
                aopMethodWrappers.add(aopMethodWrapper);
            }
        }
        registerAopMethod(aopMethodWrappers);
        return instance;
    }

    protected AopMethodWrapper loadMethod(MethodWrapper methodWrapper, Object instance){
        if (!AnnotationUtils.isPertain(methodWrapper.get(), InterceptFlag.class)){
            return null;
        }
        ClassInterceptCondition classInterceptCondition = new ClassInterceptCondition();
        MethodInterceptCondition methodInterceptCondition = new MethodInterceptCondition();

        for (ResolveIntercetConditionHandler handler : handlers) {
            handler.resolveClassCondition(classInterceptCondition, methodWrapper.getMethod());
            handler.resolveMethodCondition(methodInterceptCondition, methodWrapper.getMethod());
        }
        AopMethodWrapper aopMethodWrapper = new AopMethodWrapper(methodWrapper.get(), instance, classInterceptCondition, methodInterceptCondition);
        Sort annotation = methodWrapper.getAnnotation(Sort.class);
        if (annotation != null){
            aopMethodWrapper.setSort(annotation.value());
        }
        return aopMethodWrapper;
    }

    public static void registerAopMethod(Collection<AopMethodWrapper> methodWrappers){
        for (AopMethodWrapper aopMethodWrapper : methodWrappers) {
            String name = "aopMethodWrapper_" + aopMethodWrapper.getId();
            SpringBeanRegister.registerBean(name, aopMethodWrapper, false);
        }
    }

}
