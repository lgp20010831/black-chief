package com.black.core.work.w1;

import java.util.Map;
import java.util.concurrent.TimeUnit;

//工作流
public interface WorkFlow<R> {

    String alias();

    //执行当前工作流
    TaskGlobalListener run(Map<String, Object> param);

    int size();

    void serverBraking(TaskGlobalListener listener);

    void submitTimerTask(TaskGlobalListener listener, TimeUnit unit, long life);


    TaskGlobalListener complete(String taskId, Boolean result);
}
