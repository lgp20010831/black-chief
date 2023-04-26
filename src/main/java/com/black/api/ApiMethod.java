package com.black.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMethod {

    String responseFormat() default "";

    String requestFormat() default "";

    String[] headers() default {"Content-Type:application/json"};

    String remark() default "";
}
