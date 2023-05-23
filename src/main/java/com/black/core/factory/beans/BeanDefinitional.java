package com.black.core.factory.beans;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.agent.ProxyType;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public interface BeanDefinitional<T> {

    boolean isLazy();

    ClassWrapper<T> getClassWrapper();

    Set<MethodWrapper> getQualifiedAgentMethods();

    boolean isQualified(MethodWrapper method);

    Class<?> getPrimordialClass();

    boolean requiredAgent();

    ProxyType getProxyType();

    Class<? extends AgentLayer> getAgentLayerType();

    void setAgentLayer(AgentLayer agentLayer);

    AgentLayer getAgentLayer();

    boolean willInstanceCheckBean();

    ConstructorWrapper<?> mainConstructor() throws NoSuchMethodException;

    Set<Class<?>> getInterfaceWrappers();

    Set<Class<?>> getSuperClasses();

    boolean isPrototype();

    Map<ParameterWrapper, BeanDefinitional<?>> instanceConstructorWrapper(ConstructorWrapper<?> constructorWrapper, BeanFactory beanFactory);

    Collection<MethodWrapper> getMethods();

    Collection<FieldWrapper> getFields();

    MethodWrapper getInitMethod();

    MethodWrapper getDestroyMethod();

    MethodWrapper getCompleteMethod();

    MethodWrapper getPostConstructorMethod();

    String getBeanName();
}
