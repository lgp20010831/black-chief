package com.black.core.work.w1.time;

import java.util.concurrent.ScheduledFuture;


public interface ScheduledFutureWrapper {

    //任务句柄
    ScheduledFuture<?> future();

    //存活时间 = 延迟的毫秒数
    Long lifeTime();

    Long startTime();
}
