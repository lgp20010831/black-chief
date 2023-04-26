package com.black.core.aop.ibatis.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationsFactory {

    Query[] querys() default {};

    // -------  这里面的属性会映射到每个 query 里面
    // ------   相当于公告属性, 如果 query 里面有值则优先级高
    String groupBy() default "";

    String[] masterSlaveMapping() default {};

    String[] andOperatorQuery() default {};

    String[] orOperatorQuery() default {};

    String[] orderByAsc() default {};

    String[] orderByDesc() default {};

    String[] conditionMap() default {};
}
