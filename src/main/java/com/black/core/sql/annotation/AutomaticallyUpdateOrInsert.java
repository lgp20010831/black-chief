package com.black.core.sql.annotation;

import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 该注解通过 blent 表达式来实现如果判断数据是否存在
 * 语法:  insert[表名[表字段...]] 表示在判断添加的数据是否存在时, 匹配当前表, 根据插入数据的指定字段进行查询
 *        (如果不存在表达式, 则默认通过主键进行查询)
 *        update[表名[表字段...]] 表示在更新时, 根据指定字段当作查询条件
 *        并且不管存不存在表达式都会将 id 作为查询条件
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutomaticallyUpdateOrInsert {

    @Alias("sqlSequences")
    String[] whenAddOrUpdateSeqArray() default {};

    @Alias("setValues")
    String[] whenUpdateSetValues() default {};

    /**
     * 当当前的查询模式为 appearance 时,
     * 主表与关联表的关系通常为一对多, 但是特殊情况下, 存在数据模式一对一的情况
     * 所以这个属性就是指定那些一对一的表名
     * 主表: supplier
     * @AutomaticallyUpdateOrInsert(whenAppearanceSingleMapping = {supplier_address, supplier_bussine})
     * */
    String[] whenAppearanceSingleMapping() default {};

    String[] whenSelectCondition() default {};
}
