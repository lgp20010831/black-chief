package com.black.core.aop.servlet.nill;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 李桂鹏
 * @create 2023-07-12 15:26
 */
@SuppressWarnings("all") @Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoTextAsNull {

    boolean removeMapKey() default true;
}
