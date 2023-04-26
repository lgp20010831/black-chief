package com.black.core.sql.annotation;

import com.black.core.sql.code.mapping.EnabledGlobalMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//写这个功能是为了协助开发, 摒弃实体类的约束
//但是只能实现简单的增删改查, 如果sql是复杂的多表查询,则不会支持,还是使用 mybtais
@EnabledGlobalMapping
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledMapSQLApplication {
    //摒弃实体类, 只需要 map 就能实现添加数据和查询数据
    //在数据库字段特别多的情况下, 写实体类太痛苦了, 就算有代码自动生成也是感觉繁重
    //外部传来一组 map 数据, 希望不转化成实体类, 就能够直接操作数据库
    //这个组件就是为了实现这个, 只需要一个 mapper 类
    //同时自动开启事务, 列名和数据名开放转换, 并可以添加监听器, 尽可能做到灵活
    //同事适配多个数据源, 额外数据源的提供需要实现接口自己提供

}
