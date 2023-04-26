package com.black.core.sql.fast.annotation;

import com.black.core.annotation.UnRecommended;
import com.black.core.json.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//适合逻辑少, 需要添加大量数据的 insert 方法, 最接近 jdbc 效率
//缺点:
//      不支持创建返回影响主键的语句, 所以根本无法支持返回值, 不管定义什么类型返回值
//      返回结果总是为空

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FastInsert {

    String tableName() default "";

    @UnRecommended
    boolean parseResult() default false;

    @Alias("sqlSequences")
    String[] fragment() default {};

    //每次批次添加的数量
    int batchSize() default 2000;
}
