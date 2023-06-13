package com.black.aop.impl;

import com.black.aop.ClassInterceptCondition;
import com.black.aop.InterceptClass;
import com.black.aop.InterceptMethod;
import com.black.aop.MethodInterceptCondition;
import com.black.core.util.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:41
 */
@SuppressWarnings("all")
public class CommonInterceptConditionHandler implements ResolveIntercetConditionHandler{


    @Override
    public void resolveClassCondition(ClassInterceptCondition condition, Method method) {
        InterceptClass annotation = method.getAnnotation(InterceptClass.class);
        if (annotation != null){
            AnnotationUtils.loadAttribute(annotation, condition);
        }
    }

    @Override
    public void resolveMethodCondition(MethodInterceptCondition condition, Method method) {
        InterceptMethod annotation = method.getAnnotation(InterceptMethod.class);
        if (annotation != null){
            AnnotationUtils.loadAttribute(annotation, condition);
        }
    }
}
