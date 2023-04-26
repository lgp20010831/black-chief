package com.black.core.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurer {


    /***
     * 被该注解标注的方法会被进行解析
     * 一个标准的 mapper 方法需要标注该注解, 并且指定该方法对应的 sql 方法: @Query / @Insert / @Delete / @Renew
     * 并且需要通过 tableName 属性 或者标注 @TableName 注解来指明该方法操作的表名
     *
     * 但是通过方法名可以简化以上步骤
     * 如果方法名带有指定的前缀, 则自动判断其 sql 方法
     * 例如
     *      query..., find..., select....  --->  @Query
     *      add ..., insert...  ---> @Insert
     *      ......
     *   前缀可参考{@link com.black.core.sql.code.util.SqlNameUtils}
     *
     *   此外去掉以上前缀那么剩下的就会被认定为表名
     *   addUser   ->  user 表
     *   截取到表名会判断这个表在当前数据库存不存在, 不存在则抛异常
     *   当注解的 tableName 有值时, 也就不会通过解析方法名来找 tableName
     */


    //在最后生成的 sql 上进行拼接
    String applySql() default "";

    //表名
    String tableName() default "";


    /*
        如果时 查, 改, 删, 那么该属性的值会被作为查询条件


        如果 insert 过程中想添加默认值, 例如 del=0
        则只需要表达式 del=0
        写法: 字段名 = substring(?, 1, 2)
        xxx = now()
        字段名是列名
        该属性存在的字段, 优先级大于外部参数, 往往定义些写死的属性

     */
    String[] sqlSequences() default {};

    /*
        只有方法为 update 时才有效
        语法: column = val
     */
    String[] setValues() default {};

    String[] returnColumns() default {};
}
