package com.black.core.sql.annotation;

import com.black.core.annotation.Mapper;
import com.black.core.json.Alias;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.*;
import com.black.core.sql.code.config.ExternalConfigurer;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Mapper
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalConfiguration {

    @Alias("dataSourceAlias")
    String value() default "master";

    boolean initTable() default true;

    boolean allowScroll() default false;

    boolean openCondition() default false;

    boolean useStatementCache() default false;

    int batchStrategy() default 5000;

    Class<? extends DataSourceBuilder> builderClass() default YmlDataSourceBuilder.class;

    //java 字段和数据库字段转换
    Class<? extends AliasColumnConvertHandler> convertHandlerType() default HumpColumnConvertHandler.class;

    Class<? extends Log> logImpl() default SystemLog.class;

    Class<? extends ExternalConfigurer> externalConfigurer() default ExternalConfigurer.class;
}
