package com.black.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResolverThrowable {

    //处理哪个条目的异常, * 代表全局
    String[] value() default "*";

    //异步执行
    boolean asyn() default false;
}
