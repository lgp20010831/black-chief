package com.black.core.work.w1.cache;

import com.black.core.work.w1.TaskFlowQueue;
import com.black.core.work.w1.TaskGlobalListener;

//缓存任务不管是存数据库还是写入文件, 还是存入 redis
public interface CacheTask {

    //写入
    void writeCache(TaskGlobalListener taskGlobalListener);

    //读缓存信息
    TaskGlobalListener readTaskListener(String taskId);

    void finishTask(TaskGlobalListener listener, boolean result);

    void processorCloseServer(Long life, Long futureStart, Long serverStopTime, String taskId);

    void init(TaskFlowQueue queue);
}
