package com.black.sql_v2.javassist;

import com.black.sql_v2.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {

    String methodName() default "";

    String[] tableNames();

    String alias() default Sql.DEFAULT_ALIAS;
}
