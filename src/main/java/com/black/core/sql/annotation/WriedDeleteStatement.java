package com.black.core.sql.annotation;

import com.black.core.util.AliasMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WriedDeleteStatement {

    @AliasMapping(attribute = "wrapper")
    String tableName() default "";

    //必须存在的属性, 因为更新事关重大, 没有必要的属性限制, 怕吧所有属性更换
    String[] requiredProperties() default {};

    WriedQueryStatement wrapper() default @WriedQueryStatement;
}
