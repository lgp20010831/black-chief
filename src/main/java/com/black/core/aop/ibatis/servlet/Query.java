package com.black.core.aop.ibatis.servlet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//子查询注解, 标注在参数上

/**
 * 主要目地
 * 解决一对多的查询
 * 首先确定数据源, json下, 标注了 {@link org.springframework.web.bind.annotation.ResponseBody} 则被视为主数据源
 * 其他不确定的情况下可以根据 majorSource 指定数据源的名称
 *
 * 场景
 * a1(id ...)
 * a2(a1Id ...)  A2Mapper
 * a3(a1Id ...)
 * a4(a2Id ...)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    //返回结果 map 的 key
    String mapKey() default "";

    String groupBy() default "";

    //左边主, 右边从
    //{"id->a1Id"}
    String[] masterSlaveMapping() default {};

    //A2Mapper
    //结果映射的实体类
    Class<? extends BaseMapper<?>> mapper();

    boolean in() default true;

    //默认填充, 节省在注解上花费的功夫
    boolean defaultSet() default true;

    //a1Id
    //查询条件
    String[] andOperatorQuery() default {};

    String[] orOperatorQuery() default {};

    String[] orderByAsc() default {};

    String[] orderByDesc() default {};

    String[] conditionMap() default {};
}
