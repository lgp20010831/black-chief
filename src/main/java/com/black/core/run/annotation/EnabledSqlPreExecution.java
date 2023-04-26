package com.black.core.run.annotation;

import com.black.core.run.code.ContextSqlScanner;
import com.black.core.run.code.IbtaisSqlExecute;
import com.black.core.run.code.SQLFileExecute;
import com.black.core.run.code.SQLFileScanner;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.YmlDataSourceBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledSqlPreExecution {

    //sql 文件位置
    String[] position() default {};

    //执行sql文件的处理器
    Class<? extends SQLFileExecute> executeType() default IbtaisSqlExecute.class;

    //扫描 sql 文件获取字节流的类
    Class<? extends SQLFileScanner> scannerType() default ContextSqlScanner.class;

    //提供数据源的类, 不会关闭数据源, 且会多次调用 getDatasource, 尽量保持数据源的单例存在
    Class<? extends DataSourceBuilder> datasource() default YmlDataSourceBuilder.class;

    boolean stopOnFileError() default false;

    boolean stopOnSentenceError() default false;
}
