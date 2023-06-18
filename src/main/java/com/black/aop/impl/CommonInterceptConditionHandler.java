package com.black.aop.impl;

import com.black.aop.ClassInterceptCondition;
import com.black.aop.InterceptOnClass;
import com.black.aop.InterceptOnMethod;
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
        InterceptOnClass annotation = method.getAnnotation(InterceptOnClass.class);
        if (annotation != null){
            AnnotationUtils.loadAttribute(annotation, condition);
        }
    }

    @Override
    public void resolveMethodCondition(MethodInterceptCondition condition, Method method) {
        InterceptOnMethod annotation = method.getAnnotation(InterceptOnMethod.class);
        if (annotation != null){
            AnnotationUtils.loadAttribute(annotation, condition);
        }
    }
}
