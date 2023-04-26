package com.black.core.aop.code;

import org.springframework.aop.PointcutAdvisor;

import java.lang.reflect.Method;

public interface AopGlobalAdvisor extends PointcutAdvisor {

    void successfulMatchClass(Class<?> clazz, AopTaskManagerHybrid hybrid);

    void successfulMatchMethod(Class<?> clazz, Method method, AopTaskManagerHybrid hybrid);

}
