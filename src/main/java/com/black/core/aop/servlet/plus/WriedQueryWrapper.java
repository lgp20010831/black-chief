package com.black.core.aop.servlet.plus;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WriedWrapper
public @interface WriedQueryWrapper {

    //转换的字段名, 在参数少的时候, 会默认自己寻找
    @Alias("pointArgName")
    String value() default "";

    //如果参数需要拼接 or 运算符,则对应的实体类字段名
    String[] orConditionFields() default {};

    //如果该实体类是一个 baseBean, 是否在实例化以后调用 wriedValue() 方法
    boolean ifBaseBeanInvokeWriedDefaultValue() default true;

    //如果传的参数需要拼接模糊查询, 则对应的实体类字段...
    String[] likeConditionFields() default {};

    //语法 字段:value/或是方法
    String[] conditionMap() default {};

    //有资格作为条件的字段
    String[] qualificationAsCondition() default "*";

    //无效查询条件, 只能指定在 qualificationAsCondition 范围内的字段
    String[] invalidCondition() default {};

    //如果参数是一个array并且元素对应的了实体类字段, 那么作为条件, 有效的运算符应该为
    // in/ not in
    String ifArrayOperator() default "in";

    //如果参数是个 array， 且每个元素不能转换成实体类, 则该元素对应实体类中的字段名是什么
    String ifArrayPointFieldName() default "";

    //对于 json 里的空 value 的操作
    boolean ignoreNullValue() default true;

    //正序排序字段
    String[] orderByAsc() default {};

    String[] orderByDesc() default {};

    //在末尾拼接sql
    String applySql() default "";
}
