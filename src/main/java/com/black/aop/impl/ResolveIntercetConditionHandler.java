package com.black.aop.impl;

import com.black.aop.ClassInterceptCondition;
import com.black.aop.MethodInterceptCondition;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-06-06 16:39
 */
@SuppressWarnings("all")
public interface ResolveIntercetConditionHandler {


    void resolveClassCondition(ClassInterceptCondition condition, Method method);

    void resolveMethodCondition(MethodInterceptCondition condition, Method method);

}
