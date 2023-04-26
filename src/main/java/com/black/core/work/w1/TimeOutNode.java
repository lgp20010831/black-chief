package com.black.core.work.w1;

import java.util.concurrent.TimeUnit;

public interface TimeOutNode {

    //是否有截止时间
    default boolean isDelayed(){
        return getUnit() != null;
    }

    //获取时间单位
    TimeUnit getUnit();

    //获取截止时间
    long getDeadline();
}
