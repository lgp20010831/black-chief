package com.black.core.aop.annotation;

import com.black.core.aop.code.Premise;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AopHybrid {

    Class<? extends Premise> value() default Premise.class;
}
