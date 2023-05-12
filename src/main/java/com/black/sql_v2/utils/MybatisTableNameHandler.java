package com.black.sql_v2.utils;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author 李桂鹏
 * @create 2023-05-12 10:25
 */
@SuppressWarnings("all")
public class MybatisTableNameHandler {


    public static String getIbatisTableName(Class<?> primordialClass){
        TableName annotation = primordialClass.getAnnotation(TableName.class);
        return annotation == null ? null : annotation.value();
    }
}
