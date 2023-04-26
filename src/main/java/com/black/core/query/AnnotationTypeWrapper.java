package com.black.core.query;

import com.black.core.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationTypeWrapper {

    private static final Map<Class<? extends Annotation>, AnnotationTypeWrapper> cache = new ConcurrentHashMap<>();

    public static AnnotationTypeWrapper get(Class<? extends Annotation> annotationClass){
        return cache.computeIfAbsent(annotationClass, AnnotationTypeWrapper::new);
    }


    private final Map<String, MethodWrapper> annotationMethodCache = new HashMap<>();

    private Map<Class<? extends Annotation>, Annotation> annotationMap;

    public static final String EXCLUDE = "java.lang.annotation";

    private final Class<? extends Annotation> annotationClass;

    public AnnotationTypeWrapper(Class<? extends Annotation> annotationClass){
        for (Method declaredMethod : annotationClass.getDeclaredMethods()) {
            declaredMethod.setAccessible(true);
            annotationMethodCache.put(declaredMethod.getName(), MethodWrapper.get(declaredMethod));
        }
        this.annotationClass = annotationClass;
    }

    public Collection<MethodWrapper> getMethods(){
        return annotationMethodCache.values();
    }

    public Map<String, MethodWrapper> getAnnotationMethods() {
        return annotationMethodCache;
    }

    public Collection<MethodWrapper> getAnnotationMethodCollection(){
        return annotationMethodCache.values();
    }

    public MethodWrapper select(String name){
        return annotationMethodCache.get(name);
    }

    public Collection<MethodWrapper> filterByContainAnnotation(Class<? extends Annotation> type){
        Set<MethodWrapper> set = new HashSet<>();
        for (MethodWrapper wrapper : getAnnotationMethodCollection()) {
            if (wrapper.hasAnnotation(type)){
                set.add(wrapper);
            }
        }
        return set;
    }

    public boolean contain(String name){
        return select(name) != null;
    }

    public Class<?> getType(String name){
        MethodWrapper wrapper = select(name);
        return wrapper == null ? null : wrapper.getReturnType();
    }

    public Object getValue(String name, Annotation annotation){
        MethodWrapper wrapper = select(name);
        if (wrapper != null){
            return wrapper.invoke(annotation);
        }
        return null;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type){
        return getAnnotationMap().containsKey(type);
    }

    public int annotationSize(){
        return getAnnotations().size();
    }

    public <T> T getAnnotation(Class<T> type){
        return (T) getAnnotationMap().get(type);
    }

    public Collection<Annotation> getAnnotations(){
        return getAnnotationMap().values();
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotationMap() {
        if (annotationMap == null){
            annotationMap = new HashMap<>();
            Annotation[] annotations = annotationClass.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> type = annotation.annotationType();
                String packageName = ClassUtils.getPackageName(type);
                if (!packageName.startsWith(EXCLUDE)){
                    annotationMap.put(type, annotation);
                }
            }
        }

        return annotationMap;
    }
}
