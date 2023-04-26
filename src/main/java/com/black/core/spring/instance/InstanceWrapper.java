package com.black.core.spring.instance;


import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceWrapper<T> implements InstanceElement<T>{

    Class<T> instanceClass;

    Constructor<T> constructor;

    boolean errorConstructor = false;

    Class<?>[] superClassWrapper;

    InstanceElementFactory instanceElementFactory;

    Map<Class<?>, InstanceElement<?>> instanceElementTypeMap;

    public InstanceWrapper(Class<T> instanceClass,
                           InstanceElementFactory instanceElementFactory) {
        this.instanceClass = instanceClass;
        this.instanceElementFactory = instanceElementFactory;
    }

    @Override
    public Class<T> instanceClass() {
        return instanceClass;
    }

    @Override
    public Class<?>[] superClassWrapper() {

        if (superClassWrapper == null){
            List<Class<?>> list = new ArrayList<>();
            Class<?> targetClass = instanceClass.getSuperclass();
            for (;;){

                if (targetClass == null || targetClass.equals(Object.class))
                    break;

                list.add(targetClass);
                targetClass = targetClass.getSuperclass();
            }
            superClassWrapper = list.toArray(new Class[0]);
        }

        return superClassWrapper;
    }

    @Override
    public Class<?>[] interfaceWrapper() {
        return instanceClass.getInterfaces();
    }

    @Override
    public Map<Class<?>, InstanceElement<?>> instanceConstructorWrapper() {
        if (instanceElementTypeMap == null){
            Constructor<T> constructor = instanceConstructor();
            if (constructor == null)
                return null;
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            instanceElementTypeMap = new HashMap<>();
            for (Class<?> parameterType : parameterTypes) {
                instanceElementTypeMap.put(parameterType, instanceElementFactory.createElement(parameterType));
            }
        }
        return instanceElementTypeMap;
    }

    @Override
    public Constructor<T> instanceConstructor() {
        if (constructor == null){
            if (errorConstructor)
                return null;
            Constructor<T>[] constructors = (Constructor<T>[]) instanceClass.getConstructors();
            if (constructors.length == 1){
                constructor = constructors[0];
                return constructor;
            }
            for (Constructor<T> constructor : constructors) {
                if(constructor.isAnnotationPresent(InstanceConstructor.class))
                    return constructor;
            }
            try {
                constructor = instanceClass.getConstructor();
            } catch (NoSuchMethodException e) {
                errorConstructor = true;
                return constructor;
            }
        }
        return constructor;
    }

    @Override
    public boolean springComponent() {
        return instanceClass.isAnnotationPresent(Component.class);
    }

    @Override
    public boolean springConfig() {
        return instanceClass.isAnnotationPresent(Configuration.class);
    }

    @Override
    public String toString() {
        return "InstanceWrapper{" +
                "instanceClass=" + instanceClass +
                '}';
    }
}
