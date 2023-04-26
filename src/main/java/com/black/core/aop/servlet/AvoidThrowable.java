package com.black.core.aop.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvoidThrowable {

    //要规避的异常集合
    Class<? extends Throwable>[] value() default {};

    //成功规避异常后抛出的 runtime异常
    String runtimeMessage() default "系统异常";
}
