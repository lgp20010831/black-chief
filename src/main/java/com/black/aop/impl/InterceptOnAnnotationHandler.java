package com.black.aop.impl;

import com.black.aop.ClassInterceptCondition;
import com.black.aop.InterceptOnAnnotation;
import com.black.aop.MethodInterceptCondition;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:42
 */
@SuppressWarnings("all")
public class InterceptOnAnnotationHandler implements ResolveIntercetConditionHandler{


    @Override
    public void resolveClassCondition(ClassInterceptCondition condition, Method method) {
        InterceptOnAnnotation annotation = method.getAnnotation(InterceptOnAnnotation.class);
        if (annotation != null){
            condition.setAnnAt(annotation.value());
            condition.setAnnAnd(annotation.and());
            condition.setConnectMethodWithAnd(false);
        }
    }

    @Override
    public void resolveMethodCondition(MethodInterceptCondition condition, Method method) {
        InterceptOnAnnotation annotation = method.getAnnotation(InterceptOnAnnotation.class);
        if (annotation != null){
            condition.setAnnAt(annotation.value());
            condition.setAnnAnd(annotation.and());
        }
    }
}
