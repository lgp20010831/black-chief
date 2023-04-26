package com.black.core.sql.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenDynamicDataSourceArgurament {

    @Alias("dynamicParamName")
    String value() default "";

}
