package com.black.core.permission;

import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.Table;

public class TableNameUtils {



    public static String getTableName(Class<?> type){
        try {
            Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            TableName annotation = type.getAnnotation(TableName.class);
            if (annotation != null){
                return annotation.value();
            }
        } catch (ClassNotFoundException e) {

        }
        try {
            Class.forName("javax.persistence.Table");
            Table annotation = type.getAnnotation(Table.class);
            if (annotation != null){
                return annotation.name();
            }
        } catch (ClassNotFoundException e) {}

        return null;
    }
}
