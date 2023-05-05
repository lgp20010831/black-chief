package com.black.core.ibatis;

/**
 * @author 李桂鹏
 * @create 2023-05-05 14:01
 */
@SuppressWarnings("all")
public class EnvironmentalJudgment {


    public static boolean isMybatisEnv(){
        try {
            Class.forName("com.baomidou.mybatisplus.core.mapper.BaseMapper");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
