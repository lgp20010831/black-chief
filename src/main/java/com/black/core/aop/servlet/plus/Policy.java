package com.black.core.aop.servlet.plus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Policy {

    MappingPolicy value() default MappingPolicy.FieldName$column_name;
}
