package com.black.core.ill.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalThrowableCaptureIntercept {

    boolean writeStackHeap() default false;

    /** 当没有处理器可以干预此此异常时,自动捕获拦截 */
    boolean ifUnattendedCapture() default true;

    String captureResult() default "";

    Class<?> resultType() default String.class;
}
