package com.black.core.sql.annotation;

import com.black.core.sql.code.advance.SqlProvision;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InAdvance {

    String[] value() default {};

    Class<? extends SqlProvision> provider() default SqlProvision.class;

    boolean stopOnInvokeError() default false;

    boolean stopGlobalOnInvokeError() default false;

    boolean commitOnStop() default true;

    boolean commit() default true;
}
