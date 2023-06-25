package com.black.core.aop.servlet;


import com.black.core.query.*;

import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParameterWrapper implements Wrapper<Parameter>, GenericWrapper, ModifierInformationist {

    private final Parameter parameter;

    private ConstructorWrapper<?> constructorWrapper;

    private MethodWrapper methodWrapper;

    private final String name;

    private final int index;

    private final Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<>();

    private final Class<?> type;

    public ParameterWrapper(@NonNull Parameter parameter, int index) {
        this.parameter = parameter;
        name = parameter.getName();
        this.index = index;
        for (Annotation annotation : parameter.getAnnotations()) {
            annotationMap.put(annotation.annotationType(), annotation);
        }
        type = parameter.getType();
    }

    @Override
    public int getModifiers() {
        return get().getModifiers();
    }

    @Override
    public Type getGenericType() {
        return parameter.getParameterizedType();
    }

    public int getAnnotationSize(){
        return annotationMap.size();
    }

    public boolean isMethodParam(){
        return getExecutable() instanceof Method;
    }

    public boolean isConstructorParam(){
        return getExecutable() instanceof Constructor;
    }

    public MethodWrapper getDeclaringMethod(){
        if (isMethodParam()){
            if (methodWrapper == null){
                methodWrapper = MethodWrapper.get((Method) getExecutable());
            }
        }
        return methodWrapper;
    }

    public ConstructorWrapper<?> getDeclaringConstructor(){
        if (isConstructorParam()){
            if (constructorWrapper == null){
                constructorWrapper = ConstructorWrapper.get((Constructor<?>) getExecutable());
            }
        }
        return constructorWrapper;
    }

    public Executable getExecutable(){
        return parameter.getDeclaringExecutable();
    }

    public Parameter getParameter() {
        return parameter;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public Collection<Annotation> getAnnotations() {
        return annotationMap.values();
    }

    public Class<?> getType() {
        return type;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type){
        return annotationMap.containsKey(type);
    }

    public <T> T getAnnotation(Class<T> type){
        return (T) annotationMap.get(type);
    }

    public Set<Class<? extends Annotation>> getAnnotationTypes() {
        return annotationMap.keySet();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof Parameter) return parameter.equals(o);
        if (o instanceof ParameterWrapper){
            return ((ParameterWrapper)o).parameter.equals(parameter);
        }
        return false;
    }

    @Override
    public Parameter get() {
        return parameter;
    }


    @Override
    public String toString() {
        return "@[" + hashCode() + "]wrapper -> " + get().toString();
    }
}
