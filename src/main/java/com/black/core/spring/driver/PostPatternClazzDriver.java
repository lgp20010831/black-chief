package com.black.core.spring.driver;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.factory.ReusingProxyFactory;

import java.util.Collection;
import java.util.Map;

public interface PostPatternClazzDriver extends Driver {

    default void postPatternClazz(Class<?> beanClazz, Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                  ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication){}


    default void endPattern(Collection<Class<?>> clazzCollection, Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                            ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication){

    }

    default void finallyEndPattern(Collection<Class<?>> clazzCollection, Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                            ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication){

    }

}
