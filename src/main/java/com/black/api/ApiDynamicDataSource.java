package com.black.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDynamicDataSource {

    String name() default "";

    ApiDynamicType type() default ApiDynamicType.CHIEF;
}
