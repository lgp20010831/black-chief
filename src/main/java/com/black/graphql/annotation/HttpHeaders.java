package com.black.graphql.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpHeaders {

    @Alias("headerName")
    String value() default "";

    String[] headers() default {};


}
