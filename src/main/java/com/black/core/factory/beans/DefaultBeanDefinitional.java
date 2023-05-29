package com.black.core.factory.beans;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.agent.BeanProxy;
import com.black.core.factory.beans.agent.ProxyType;
import com.black.core.factory.beans.annotation.AsLazy;
import com.black.core.factory.beans.annotation.CompleteMethod;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.instance.InstanceConstructor;
import com.black.core.spring.instance.PostConstr;
import com.black.utils.NameUtil;
import com.black.utils.ProxyUtil;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;

@Log4j2 @SuppressWarnings("all")
public class DefaultBeanDefinitional<B> implements BeanDefinitional<B>{

    private final ClassWrapper<B> classWrapper;

    private final boolean prototype;

    private Boolean agent;

    private String beanName;

    private Class<? extends AgentLayer> layerType;

    private AgentLayer agentLayer;

    private ProxyType proxyType;

    private Set<MethodWrapper> qualifiedAgentMethods;

    public DefaultBeanDefinitional(@NonNull Class<B> clazz) {
        this(clazz, false);
    }

    public DefaultBeanDefinitional(@NonNull Class<B> clazz, boolean prototype) {
        this.prototype = prototype;
        classWrapper = ClassWrapper.get(clazz);
    }

    public boolean isPrototype() {
        return prototype;
    }

    @Override
    public boolean isLazy() {
        return com.black.core.util.AnnotationUtils.isPertain(classWrapper.get(), AsLazy.class);
    }

    @Override
    public ClassWrapper<B> getClassWrapper() {
        return classWrapper;
    }

    @Override
    public Class<?> getPrimordialClass() {
        return classWrapper.getPrimordialClass();
    }

    @Override
    public Set<MethodWrapper> getQualifiedAgentMethods() {
        if (qualifiedAgentMethods == null){
            qualifiedAgentMethods = new HashSet<>();
            if (requiredAgent()){
                qualifiedAgentMethods.addAll(classWrapper.getMethods());
            }else {
                for (MethodWrapper method : classWrapper.getMethods()) {
                    if (!(ProxyUtil.isObjectMethod(method.getName()) || ProxyUtil.isObjectMethod(method.getMethod())) &&
                        method.hasAnnotation(AgentRequired.class)){
                        qualifiedAgentMethods.add(method);
                    }
                }
            }
        }
        return qualifiedAgentMethods;
    }

    @Override
    public boolean isQualified(MethodWrapper method) {
        return getQualifiedAgentMethods().contains(method);
    }

    @Override
    public boolean requiredAgent() {
        if(agent != null) return agent;
        boolean proxy = classWrapper.hasAnnotation(AgentRequired.class);
        if (!proxy){
            loop: for (MethodWrapper mw : classWrapper.getMethods()) {
                for (Annotation annotation : mw.getAnnotationMap().values()) {
                    String name = annotation.annotationType().getName();
                    if (name.startsWith("com.black.core.factory.beans")){
                        proxy = true;
                        break;
                    }
                }
                if(!proxy){
                    for (ParameterWrapper parameterWrapper : mw.getParameterArray()) {
                        for (Annotation annotation : parameterWrapper.getAnnotations()) {
                            String name = annotation.annotationType().getName();
                            if (name.startsWith("com.black.core.factory.beans")){
                                proxy = true;
                                break loop;
                            }
                        }
                    }
                }
            }
        }
        return agent = proxy;
    }

    @Override
    public ProxyType getProxyType() {
        if(proxyType == null){
            AgentRequired annotation = classWrapper.getAnnotation(AgentRequired.class);
            if (annotation != null){
                proxyType = annotation.proxyType();
            }else
                proxyType = ProxyType.INITIALIZATION;
        }
        return proxyType;
    }

    @Override
    public Class<? extends AgentLayer> getAgentLayerType() {
        if (!requiredAgent()){
            return null;
        }
        if (layerType == null){
            AgentRequired annotation = classWrapper.getAnnotation(AgentRequired.class);
            if (annotation != null){
                layerType = annotation.value();
            }else
            layerType = BeanProxy.class;
        }
        return layerType;
    }

    @Override
    public void setAgentLayer(AgentLayer agentLayer) {
        this.agentLayer = agentLayer;
    }

    public AgentLayer getAgentLayer() {
        return agentLayer;
    }

    @Override
    public boolean willInstanceCheckBean() {
        return classWrapper.isSoild();
    }

    @Override
    public ConstructorWrapper<?> mainConstructor() throws NoSuchMethodException {
        ConstructorWrapper<?> constructorWrapper = classWrapper.getConstructorByAnnotation(MainConstructor.class);
        if (constructorWrapper != null){
            return constructorWrapper;
        }
        ConstructorWrapper<?> constructorByAnnotation = classWrapper.getConstructorByAnnotation(InstanceConstructor.class);
        if (constructorByAnnotation != null){
            return constructorByAnnotation;
        }
        ConstructorWrapper<?> constructor = classWrapper.getConstructor();
        if (constructor == null){
            List<ConstructorWrapper<?>> constructorWrapperList = classWrapper.getConstructorWrapperList();
            if (constructorWrapperList.size() == 1){
                return constructorWrapperList.get(0);
            }
            throw new NoSuchMethodException();
        }
        return constructor;
    }

    @Override
    public Set<Class<?>> getInterfaceWrappers() {
        return classWrapper.getInterfaces();
    }

    @Override
    public Set<Class<?>> getSuperClasses() {
        Set<Class<?>> superClasses = new HashSet<>();
        Class<?> superClass = classWrapper.getSuperClass();
        while (superClass != null){
               superClasses.add(superClass);
               superClass = superClass.getSuperclass();
        }
        return superClasses;
    }

    @Override
    public Map<ParameterWrapper, BeanDefinitional<?>> instanceConstructorWrapper(ConstructorWrapper<?> constructorWrapper, BeanFactory factory){
        Map<ParameterWrapper, BeanDefinitional<?>> definitionalMap = new HashMap<>();
        for (ParameterWrapper parameterWrapper : constructorWrapper.getParameterWrappers()) {
            Class<?> type = parameterWrapper.getType();
            if (ClassWrapper.isBasic(type.getName())){
                type = ClassWrapper.pack(type.getName());
            }
            definitionalMap.put(parameterWrapper, factory.createDefinitional(type, false));
        }

        return definitionalMap;
    }

    @Override
    public Collection<MethodWrapper> getMethods() {
        return classWrapper.getMethods();
    }

    @Override
    public Collection<FieldWrapper> getFields() {
        return classWrapper.getFields();
    }

    @Override
    public MethodWrapper getInitMethod() {
        return classWrapper.getSingleMethodByAnnotation(InitMethod.class);
    }

    @Override
    public MethodWrapper getDestroyMethod() {
        return classWrapper.getSingleMethodByAnnotation(DestroyMethod.class);
    }

    @Override
    public MethodWrapper getCompleteMethod() {
        return classWrapper.getSingleMethodByAnnotation(CompleteMethod.class);
    }

    @Override
    public MethodWrapper getPostConstructorMethod() {
        MethodWrapper wrapper = classWrapper.getSingleMethodByAnnotation(PostConstructor.class);
        if (wrapper == null){
            wrapper = classWrapper.getSingleMethodByAnnotation(PostConstr.class);
        }
        return wrapper;
    }

    @Override
    public String getBeanName() {
        if (beanName == null){
            Class<B> primordialClass = classWrapper.getPrimordialClass();
            BeanName name = AnnotationUtils.getAnnotation(primordialClass, BeanName.class);
            if(name == null){
                beanName = NameUtil.getName(primordialClass);
            }else {
                beanName = name.value();
            }
        }
        return beanName;
    }
}
