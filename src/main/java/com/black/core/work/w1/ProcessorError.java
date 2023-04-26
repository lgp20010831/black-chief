package com.black.core.work.w1;

public interface ProcessorError {

    //处理异常, 返回值为 true 时, 任务继续, 返回值为 fasle 任务中止
    boolean handlerThrowable(Throwable throwable, TaskGlobalListener listener, TaskFlowQueue queue);

}
