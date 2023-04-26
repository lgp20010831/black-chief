package com.black.core.query;

import com.black.core.aop.servlet.ParameterWrapper;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConstructorWrapper<B> implements Wrapper<Constructor<B>>, ExecutableWrapper{

    static final Map<Constructor<?>, ConstructorWrapper<?>> cache = new ConcurrentHashMap<>();

    public static <B> ConstructorWrapper<B> get(Constructor<B> cl){
        return (ConstructorWrapper<B>) cache.computeIfAbsent(cl, ConstructorWrapper::new);
    }


    private final Constructor<B> constructor;

    private ClassWrapper<?> classWrapper;

    private final Map<Class<? extends Annotation>, Annotation> annotationMap = new ConcurrentHashMap<>();

    private final Map<String, ParameterWrapper> parameterWrappers = new ConcurrentHashMap<>();

    public ConstructorWrapper(@NonNull Constructor<B> constructor) {
        this.constructor = constructor;
        Annotation[] annotations = constructor.getAnnotations();
        for (Annotation annotation : annotations) {
            annotationMap.put(annotation.annotationType(), annotation);
        }
        Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            parameterWrappers.put(parameter.getName(), new ParameterWrapper(parameter, i));
        }
    }

    public ClassWrapper<?> getDeclaringClass(){
        if (classWrapper == null){
            classWrapper = ClassWrapper.get(constructor.getDeclaringClass());
        }
        return classWrapper;
    }

    public int getParamCount(){
        return constructor.getParameterCount();
    }

    public boolean hasAnnotation(Class<? extends Annotation> type){
        return annotationMap.containsKey(type);
    }

    public <T extends Annotation> T getAnnotation(Class<T> type){
        return (T) annotationMap.get(type);
    }

    public Collection<ParameterWrapper> getParameterWrappers() {
        return parameterWrappers.values();
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotationMap() {
        return annotationMap;
    }

    public Set<String> getParamNames(){
        return parameterWrappers.keySet();
    }

    public B newInstance(Object... args){
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?>[] getParameterTypes(){
        return constructor.getParameterTypes();
    }

    public Constructor<B> getConstructor() {
        return constructor;
    }

    public ParameterWrapper querySingleParameterByType(Class<?> type){
        List<ParameterWrapper> parameterWrappers = queryParameterByType(type);
        return parameterWrappers == null || parameterWrappers.isEmpty() ? null : parameterWrappers.get(0);
    }

    public List<ParameterWrapper> queryParameterByType(Class<?> type){
        List<ParameterWrapper> wrappers = new ArrayList<>();
        for (ParameterWrapper parameterWrapper : getParameterWrappers()) {
            if (type.isAssignableFrom(parameterWrapper.getType())){
                wrappers.add(parameterWrapper);
            }
        }
        return wrappers;
    }

    public ParameterWrapper queryParam(String name){
        return parameterWrappers.get(name);
    }

    public ParameterWrapper querySingleParameterByAnnotation(Class<? extends Annotation> type){
        List<ParameterWrapper> parameterWrappers = queryParameterByAnnotation(type);
        return parameterWrappers == null || parameterWrappers.isEmpty() ? null : parameterWrappers.get(0);
    }

    public List<ParameterWrapper> queryParameterByAnnotation(Class<? extends Annotation> type){
        List<ParameterWrapper> wrappers = new ArrayList<>();
        for (ParameterWrapper parameterWrapper : parameterWrappers.values()) {
            if (parameterWrapper.hasAnnotation(type)) {
                wrappers.add(parameterWrapper);
            }
        }
        return wrappers;
    }

    @Override
    public int hashCode() {
        return constructor.hashCode();
    }

    @Override
    public Constructor<B> get() {
        return constructor;
    }

    @Override
    public String toString() {
        return "@[" + hashCode() + "]wrapper -> " + get().toString();
    }
}
