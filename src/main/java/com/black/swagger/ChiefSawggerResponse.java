package com.black.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChiefSawggerResponse {

    //结果是不是数组
    boolean list() default false;

    //执行的方法
    String value() default "";

    //直接命中实体类
    Class<?> target() default void.class;
}
