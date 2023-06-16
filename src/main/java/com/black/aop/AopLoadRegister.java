package com.black.aop;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.mvc.SpringBeanRegister;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-06-16 13:45
 */
@SuppressWarnings("all")
public class AopLoadRegister {

    private static AopLoadRegister register;

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
        return new AopMethodWrapper(methodWrapper.get(), instance, classInterceptCondition, methodInterceptCondition);
    }

    public static void registerAopMethod(Collection<AopMethodWrapper> methodWrappers){
        for (AopMethodWrapper aopMethodWrapper : methodWrappers) {
            SpringBeanRegister.registerBean(aopMethodWrapper, false);
        }
    }

}
