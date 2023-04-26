package com.black.core.native_sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledNativeAllocationStrategy {


    //使用策略表达式 blend 语法
    //spring[default, beanName], mapSql[master, mes]
    //支持的两个平面 spring 和 mapSql 之后可以加入 mybatis
    //default =  spring 唯一管理的 数据源
    //该值的默认情况就是获取 spring 默认管理的数据源
    String value() default "spring[default]";

}
