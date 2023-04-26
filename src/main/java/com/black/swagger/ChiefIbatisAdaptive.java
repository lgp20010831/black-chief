package com.black.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChiefIbatisAdaptive {

    //获取类的方法的表达式
    String value() default "";

    Class<?> target() default void.class;
}
