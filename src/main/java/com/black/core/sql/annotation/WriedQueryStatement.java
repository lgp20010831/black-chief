package com.black.core.sql.annotation;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriedQueryStatement {

    //表名
    String tableName() default "";

    //默认寻找唯一的那个数据源
    String dataSourceAlias() default "";

    //如果参数需要拼接 or 运算符,则对应的实体类字段名
    String[] orConditionFields() default {};

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

    boolean dynamic() default false;

    Class<? extends AliasColumnConvertHandler> convertHandlerType() default HumpColumnConvertHandler.class;

    //正序排序字段
    String[] orderByAsc() default {};

    String[] orderByDesc() default {};

    //在末尾拼接sql
    String applySql() default "";
}
