package com.black.aop;

import java.lang.annotation.*;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:24
 */
@SuppressWarnings("all")
@InterceptFlag
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptClass {

    Class<?>[] type() default {};

    Class<? extends Annotation>[] annAt() default {};

    boolean typeAnd() default false;

    boolean annAnd() default false;

    //是否必须 class 先通过条件, method 才能进行判断
    boolean connectMethodWithAnd() default true;
}
