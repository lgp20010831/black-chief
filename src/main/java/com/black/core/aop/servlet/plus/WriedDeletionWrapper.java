package com.black.core.aop.servlet.plus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@WriedWrapper
public @interface WriedDeletionWrapper {

    String value() default "";

    String[] autoInjection() default {};

    WriedQueryWrapper queryWrapper() default @WriedQueryWrapper;
}
