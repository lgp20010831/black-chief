package com.black.swagger.v2;

import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptDataSource {

    Class<? extends DataSourceBuilder> value() default SpringDataSourceBuilder.class;
}
