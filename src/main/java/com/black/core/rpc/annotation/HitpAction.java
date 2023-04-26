package com.black.core.rpc.annotation;

import com.black.rpc.annotation.Actuator;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Actuator
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HitpAction {

    @AliasFor(annotation = Actuator.class)
    String value() default "";

}
