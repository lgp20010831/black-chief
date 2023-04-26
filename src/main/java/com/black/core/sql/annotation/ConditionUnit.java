package com.black.core.sql.annotation;

import com.black.condition.inter.Condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionUnit {

    //条件实例
    Class<? extends Condition>[] condition() default {};

    //条件表达式
    String[] expression() default {};

    //条件符合时
    String[] then() default {};

    //条件不符合时
    String[] orElse() default {};
}
