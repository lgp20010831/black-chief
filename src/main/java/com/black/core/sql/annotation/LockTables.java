package com.black.core.sql.annotation;

import com.black.core.sql.lock.LockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LockTables {

    String[] value() default {};

    LockType type() default LockType.EXCLUSIVE;

}
