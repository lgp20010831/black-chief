package com.black.core.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDescribe {

    /** 响应信息描述 */
    String responseDescribe() default "";

    /** 请求信息描述 */
    String requestDescribe() default "";
}
