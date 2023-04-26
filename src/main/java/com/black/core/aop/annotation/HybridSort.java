package com.black.core.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HybridSort {

    /** 值越大越接近方法的真正执行
     *  值越大离方法越近
     * */
    int value();
}
