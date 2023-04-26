package com.black.core.work.w2.connect.time;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class FlowTask implements Runnable{
    String nodeId;
    WorkflowInstanceListener listener;
    WorkflowProcessing queue;
    WorkflowScheduledTaskDispatcher dispatcher;

    @Override
    public void run() {
        String id = listener.getInstance().id();

        if (log.isInfoEnabled()) {
            log.info("定时任务执行, 实例id:{}", id);
        }
        //如果进行了该任务, 则表示时间点内没有执行任务, 任务没有被取消
        //回调调度器
        dispatcher.callBackDispatcher(listener, queue, nodeId);

        //将任务走向下一个节点
        queue.complete(id, nodeId, false);
    }
}
