package com.black.core.aop.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredVal {

    //指向 json body 里的参数
    String value() default "";

    //是否允许为空
    boolean allowNull() default false;

    boolean required() default true;
}
