package com.black.core.aop.servlet.plus;

import com.black.core.aop.servlet.HttpMethodWrapper;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Setter @Getter
public class PlusMethodWrapper {
    //该类所有的注解
    Map<Class<? extends Annotation>, Annotation> annotationMap = new ConcurrentHashMap<>();
    WriedQueryWrapper queryWrapper;
    Class<?> entryClass;
    Method method;
    int argIndex = -1;
    int wrapperIndex = -1;
    String pointParamName;
    Parameter wrapperParamter;
    Parameter argParamter;
    HttpMethodWrapper httpMethodWrapper;
    boolean effective = false;
    public PlusMethodWrapper(HttpMethodWrapper httpMethodWrapper) {
        this.httpMethodWrapper = httpMethodWrapper;
    }
    public void registerAnnotation(Annotation[] annotations){
        for (Annotation annotation : annotations) {
            annotationMap.put(annotation.annotationType(), annotation);
        }
    }
    public void end(){
        effective = entryClass != null && argIndex != -1 && wrapperIndex != -1;
    }

}
