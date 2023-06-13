package com.black.aop;

import java.lang.annotation.*;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:27
 */
@SuppressWarnings("all")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptMethod {

    //需要指定方法上带有指定的注解
    Class<? extends Annotation>[] annAt() default {};

    //需要支持方法某个参数中带有指定的注解
    Class<? extends Annotation>[] paramAt() default {};

    //只支持方法的返回值类型数组
    Class<?>[] supportReturnType() default {};

    //是否要求方法必须公开
    boolean openMethod() default false;

    boolean annAnd() default false;
}
