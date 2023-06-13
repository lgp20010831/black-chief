package com.black.aop;

import java.lang.annotation.*;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:33
 */
@SuppressWarnings("all")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptOnAnnotation {

    Class<? extends Annotation>[] value() default {};

    boolean and() default false;

}
