package com.black.core.sql.annotation;

import com.black.core.json.Alias;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.InheritGlobalConvertHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunScript {

    //如果存在多个查询语句, 则结果只会返回最后一条
    @Alias("sqls")
    String[] value();

    //当一条发生异常, 中止其他条数执行
    boolean stopOnError() default true;

    boolean autoCommit() default false;

    //2022-10-20 将默认别名列名处理器(原先: HumpColumnConvertHandler) 改为 InheritGlobalConvertHandler(继承遵守所在数据源 mapper)
    Class<? extends AliasColumnConvertHandler> convertType() default InheritGlobalConvertHandler.class;


}
