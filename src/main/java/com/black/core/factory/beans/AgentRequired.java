package com.black.core.factory.beans;


import com.black.core.factory.beans.agent.BeanProxy;
import com.black.core.factory.beans.agent.ProxyType;
import com.black.core.spring.factory.AgentLayer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentRequired {


    Class<? extends AgentLayer> value() default BeanProxy.class;


    ProxyType proxyType() default ProxyType.INSTANCE_PROXY;

}
