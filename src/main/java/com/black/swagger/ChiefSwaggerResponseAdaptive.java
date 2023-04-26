package com.black.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChiefSwaggerResponseAdaptive {

    //结果是不是数组
    boolean list() default false;

    String value() default "";

    Class<?> target() default void.class;
}
