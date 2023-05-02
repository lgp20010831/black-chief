package com.black.core.annotation;

import com.black.core.aop.listener.EnableGlobalAopChainWriedModular;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EnableGlobalAopChainWriedModular
@OpenChiefApplication
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenChiefAndSupportAop {


}
