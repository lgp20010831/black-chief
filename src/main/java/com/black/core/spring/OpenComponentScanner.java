package com.black.core.spring;

import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.ReusingProxyFactory;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public class OpenComponentScanner implements PostPatternClazzDriver {

    Collection<Class<? extends OpenComponent>> openComponents;
    @Override
    public void postPatternClazz(Class<?> beanClazz,
                                 Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                 ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        if (beanClazz.isInterface() || Modifier.isAbstract(beanClazz.getModifiers()) || beanClazz.isEnum()){
            return;
        }
        if (openComponents == null){
            openComponents = chiefExpansivelyApplication.obtainEarlyComponentClazzList();
        }
        if (OpenComponent.class.isAssignableFrom(beanClazz)){
           openComponents.removeIf(component -> component.isAssignableFrom(beanClazz));
           openComponents.add((Class<? extends OpenComponent>) beanClazz);
        }
    }
}
