package com.black.core.aop.servlet.item.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***、
 * 类需要被 aop 管理
 * 标注在方法上, 代表此方法支持la表达式解析
 * 然后可以配合{@link com.black.core.aop.servlet.item.OpenItemTrigger}
 * {@link LAExpression} 进行表达式编程
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LASupportedMethod {

    //定义局部变量
    //格式: key=value
    String[] defineVariables() default {};

}
