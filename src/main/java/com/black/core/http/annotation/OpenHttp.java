package com.black.core.http.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenHttp {

    //地址前缀
    @Alias("prefix")
    String value() default "";

}
