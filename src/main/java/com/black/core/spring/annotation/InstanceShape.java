package com.black.core.spring.annotation;

import com.black.bin.InstanceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InstanceShape {


    InstanceType value() default InstanceType.INSTANCE;

}
