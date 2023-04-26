package com.black.core.work.w2.connect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WorkflowDefinition {

    //唯一别名
    String value();

    //是否挂起
    boolean hang() default false;
}
