package com.black.sql_v2.utils;

/**
 * @author 李桂鹏
 * @create 2023-05-12 10:23
 */
@SuppressWarnings("all")
public class MybatisEnv {


    public static boolean isMybatisEnv(){
        try {
            Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
