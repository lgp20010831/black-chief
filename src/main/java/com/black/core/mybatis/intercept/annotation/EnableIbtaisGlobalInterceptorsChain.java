package com.black.core.mybatis.intercept.annotation;



import com.black.core.mybatis.EnableIbatisInterceptsDispatcher;
import com.black.core.mybatis.source.annotation.EnableDynamicallyMultipleClients;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableDynamicallyMultipleClients
@EnableIntegratingIbtaisIntercepts
@EnableIbatisInterceptsDispatcher
@Deprecated
public @interface EnableIbtaisGlobalInterceptorsChain {


}
