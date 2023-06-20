package com.black.aop;

import com.black.core.aop.code.HijackObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:35
 */
@SuppressWarnings("all") @AllArgsConstructor @Getter @Setter
public class Handle {

    private volatile int invokeCount = 0;

    private Object lastResult;

    private final Class<?> clazz;

    private final Method method;

    private final Object[] args;

    private final Object proxy;

    private final MethodInvocation methodInvocation;

    public Handle(Class<?> clazz, Method method, Object[] args, Object proxy, MethodInvocation methodInvocation) {
        this.clazz = clazz;
        this.method = method;
        this.args = args;
        this.proxy = proxy;
        this.methodInvocation = methodInvocation;
    }

    public synchronized Object invoke(){
        try {
            invokeCount++;
            lastResult = methodInvocation.proceed();
            return lastResult;
        } catch (Throwable e) {
            throw new AopSecondaryInterceptionException(e);
        }
    }

}
