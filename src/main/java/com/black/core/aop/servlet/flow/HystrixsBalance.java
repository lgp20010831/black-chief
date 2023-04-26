package com.black.core.aop.servlet.flow;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HystrixsBalance {

    FlowTimeUnit[] units() default {FlowTimeUnit.MINUTES, FlowTimeUnit.DAYS, FlowTimeUnit.HOURS, FlowTimeUnit.MONTHS, FlowTimeUnit.YEARS};

    //限流表达式, min: 90 -- 表示每分钟最大访问次数为 90 次
    //           second: xxxx, hour:xxx, 前面的单位必需是 units 中存在的
    String[] limitExpression() default {};

    String[] userLimitExpression() default {};

    //打印访问信息
    boolean print() default true;

    Class<? extends Supplier<Object>> limitResponse() default DefaultLimitResponse.class;

}
