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

    private final Class<?> clazz;

    private final Method method;

    private final Object[] args;

    private final Object proxy;

    private final MethodInvocation methodInvocation;

    public Object invoke(){
        try {
            return methodInvocation.proceed();
        } catch (Throwable e) {
            throw new AopSecondaryInterceptionException(e);
        }
    }
}
