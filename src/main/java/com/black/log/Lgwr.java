package com.black.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 李桂鹏
 * @create 2023-06-06 13:41
 */
@SuppressWarnings("all")
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lgwr {

    String value() default "fetch finish network interface: ${url} on controller: ${controllerName}(${methodName})";

}
