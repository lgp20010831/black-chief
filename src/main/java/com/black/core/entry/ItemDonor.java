package com.black.core.entry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemDonor {

    //作为类中所有方法条目的前缀
    //默认为空情况下, 是没有前缀的
    //在有前缀的情况下
    //例如前缀: user
    //方法条目: query
    //那么条目连接符: -
    //最终可以解释的条目: user-query()
    String value() default "";

    //此属性为 true 情况下, 此类中的所有方法
    //不用添加@ItemMappping
    //自动作为映射方法, 条目名为方法名
    //条目方法一定不支持重载方法
    //所以说每一个方法名要保证不能重复
    boolean includeAllMethods() default false;


    boolean processingExecution() default false;
}
