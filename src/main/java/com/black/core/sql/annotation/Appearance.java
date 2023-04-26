package com.black.core.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Appearance {

    String appearanceName() default "";

    String[] sqlSequences() default {};

    String tableName();

    String foreignKeyName() default "";

    String applySql() default "";

    String[] setValues() default {};

}
