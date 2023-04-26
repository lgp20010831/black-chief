package com.black.core.aop.servlet.result;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChiefResponseAdvice {

    Class<? extends ChiefBeforeWriteResolver>[] value() default {};
}
