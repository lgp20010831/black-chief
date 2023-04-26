package com.black.swagger;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChiefMapSqlAdaptive {

    @Alias("tableName")
    String value() default "";

    String mappingTableNameMethodName() default "";

    boolean list() default false;
}
