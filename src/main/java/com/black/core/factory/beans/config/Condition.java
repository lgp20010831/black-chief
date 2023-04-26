package com.black.core.factory.beans.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {

    //返回 true 则加载, false 不加载
    Class<? extends BeanCondition> value();
}
