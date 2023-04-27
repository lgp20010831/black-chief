package com.black.sql_v2.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shkstart
 * @create 2023-04-27 14:49
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomSerialize {

    Class<? extends AvailCustomSerializer> value();

}
