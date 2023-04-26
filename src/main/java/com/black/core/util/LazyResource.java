package com.black.core.util;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;

import javax.annotation.Resource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Lazy
@Resource
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyResource {

    @AliasFor(attribute = "name", annotation = Resource.class)
    String value() default "";
}
