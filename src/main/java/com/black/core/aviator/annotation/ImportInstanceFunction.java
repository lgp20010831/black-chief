package com.black.core.aviator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 李桂鹏
 * @create 2023-05-10 10:43
 */
@SuppressWarnings("all")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportInstanceFunction {


    String value() default "";

}
