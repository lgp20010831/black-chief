package com.black.core.sql.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateOrInsert {

    @Alias("sqlSeqs")
    String[] value() default {};

    //根据哪些字段判断添加时这条数据存在
    String[] addingExist() default {};

    //在判断是否存在的 sql 中额外的条件
    String[] existCondition() default {};
}
