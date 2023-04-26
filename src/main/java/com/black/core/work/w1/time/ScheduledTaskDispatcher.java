package com.black.core.work.w1.time;

import com.black.core.work.w1.TaskFlowQueue;
import com.black.core.work.w1.TaskGlobalListener;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface ScheduledTaskDispatcher {

    //调度任务
    void doDispatcher(TaskGlobalListener listener, TaskFlowQueue queue, TimeUnit unit, long time);

    //取消一个任务
    void cancel(TaskGlobalListener listener);

    //任务在执行时会回调调度器
    void callBackDispatcher(TaskGlobalListener listener, TaskFlowQueue queue);

    Map<String, ScheduledFutureWrapper> getFutureCache();
}
