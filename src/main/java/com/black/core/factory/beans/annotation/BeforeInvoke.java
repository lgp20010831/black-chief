package com.black.core.factory.beans.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeInvoke {

    //方法的唯一别名: 方法名称|参数个数
    @Alias("methodAlias")
    String value() default "";



}
