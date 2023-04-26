package com.black.core.aop.servlet.plus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WriedEntity {

    //如果注入类型, 是个map， 则该值指定了 map 的key是什么字段
    String value() default "";
}
