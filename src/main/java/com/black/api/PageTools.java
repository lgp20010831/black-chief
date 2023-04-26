package com.black.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageTools {

    //标记在注解上, 标识该注解是一个帮助分页的注解
    String pageSize() default "";

    String pageNum() default "";
}
