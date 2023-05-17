package com.black.callback.develop;

/**
 * @author 李桂鹏
 * @create 2023-05-17 14:05
 */
@SuppressWarnings("all")
public interface DevelopmentFilter {


    boolean intercept(DevelopmentContext context, Developer developer);

}
