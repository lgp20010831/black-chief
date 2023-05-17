package com.black.callback.develop;

/**
 * @author 李桂鹏
 * @create 2023-05-17 14:12
 */
@SuppressWarnings("all")
public interface RecordDeveloper {


    void record(DevelopmentContext context, Class<?> source);


    default void finish(){

    }
}
