package com.black.core.factory.beans.config_collect520;

import java.lang.annotation.*;

@SuppressWarnings("all")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Collect {

    Class<? extends ClassMatchCustom>[] customs() default {};

    //目标所携带的注解
    Class<? extends Annotation>[] annotationAt() default {};

    boolean annotationOr() default true;

    //目标继承的类型
    Class<?>[] type() default {};

    boolean sort() default true;

    boolean typeOr() default true;

    //目标是可实例化的
    boolean soild() default true;

    //自动实例化
    boolean instance() default true;

    //搜索范围
    String[] scope() default {};

    String key() default "";

    //舍弃无法实例化的
    boolean abandonUnableInstance() default false;

    boolean prototypeCreate() default false;
}
