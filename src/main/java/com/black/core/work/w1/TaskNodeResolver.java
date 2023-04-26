package com.black.core.work.w1;

public interface TaskNodeResolver<R> {


    //判断该处理器是否能够处理该类型
    boolean support(TaskType taskType);

    //处理该节点
    R processor(TaskType taskType, TaskGlobalListener listener, TaskFlowQueue queue) throws WorkBreakException;
}
