package com.black.xml;

import com.black.sql_v2.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 李桂鹏
 * @create 2023-05-08 13:59
 */
@SuppressWarnings("all")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlMapper {

    String[] value() default {};

    String alias() default Sql.DEFAULT_ALIAS;
}
