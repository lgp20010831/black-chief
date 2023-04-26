package com.black.core.work.w2.connect.tracker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackerIdentifying {

    //要监听的工作流模板的名字
    String[] value() default {};

}
