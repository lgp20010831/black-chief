package com.black.core.work.w2.connect.time;


import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface WorkflowScheduledTaskDispatcher {

    //调度任务
    void doDispatcher(WorkflowInstanceListener listener, String nodeId, WorkflowProcessing queue, TimeUnit unit, long time);

    //取消一个任务
    void cancel(WorkflowInstanceListener listener, String nodeId);

    //任务在执行时会回调调度器
    void callBackDispatcher(WorkflowInstanceListener listener, WorkflowProcessing queue, String nodeId);

    Map<String, List<ScheduledNodeFutureWrapper>> getFutureCache();

    boolean containTask(String instanceId);

    List<ScheduledNodeFutureWrapper> getFutureTasksByInstanceId(String instanceId);

    void cancelTasks(String instanceId);
}
