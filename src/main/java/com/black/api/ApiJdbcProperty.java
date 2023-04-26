package com.black.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiJdbcProperty {

    String response() default "";

    String request() default "";

    String[] httpHeaders() default {"Content-Type:application/json"};

    String remark() default "";

    boolean hide() default false;
}
