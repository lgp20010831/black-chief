package com.black.core.aop.servlet.plus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hp
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WriedWrapper
public @interface WriedUpdateWrapper {

    //转换的字段名, 在参数少的时候, 会默认自己寻找
    String value() default "";

    //必须存在的属性, 因为更新事关重大, 没有必要的属性限制, 怕吧所有属性更换
    String[] requiredProperties() default {};

    //条件参数, 默认主键
    String[] condition() default {};

    //要更新的所有值, 在传参的基础上在指定
    String[] setFields() default {"*"};


    //需要自动填充的字段名
    //指定了字段,但字段必须要符合要求
    //要么字段上标注了 defaultValue 注解
    //要么字段是个时间类型, 则会设置为当前时间
    //如果格式是 字段名:value 则会将 value 注入
    String[] autoInjection() default {};

    //where 条件取值
    WriedQueryWrapper queryWrapper() default @WriedQueryWrapper;
}
