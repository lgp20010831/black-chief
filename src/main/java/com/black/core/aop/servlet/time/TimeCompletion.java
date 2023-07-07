package com.black.core.aop.servlet.time;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 李桂鹏
 * @create 2023-06-27 14:39
 */
@SuppressWarnings("all")
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeCompletion {
    String[] value() default {};

    String appendStartTime() default " 00:00:00";

    String appendEndTime() default " 23:59:59";

}
