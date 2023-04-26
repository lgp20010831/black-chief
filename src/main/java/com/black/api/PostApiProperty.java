package com.black.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiJdbcProperty
@RequestMapping(method = RequestMethod.POST)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostApiProperty {

    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] url() default "";

    @AliasFor(annotation = ApiJdbcProperty.class)
    String response() default "";

    @AliasFor(annotation = ApiJdbcProperty.class)
    String request() default "";

    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    @AliasFor(annotation = ApiJdbcProperty.class)
    String[] httpHeaders() default {"Context-Type:application/json"};

    @AliasFor(annotation = ApiJdbcProperty.class)
    String remark() default "";

    @AliasFor(annotation = ApiJdbcProperty.class)
    boolean hide() default false;

    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};
}
