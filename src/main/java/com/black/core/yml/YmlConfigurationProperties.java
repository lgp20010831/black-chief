package com.black.core.yml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface YmlConfigurationProperties {
    /** 前缀 */
    String value() default "";

    boolean wriedFiled() default false;
}
