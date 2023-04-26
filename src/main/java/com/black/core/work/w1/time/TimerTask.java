package com.black.core.work.w1.time;

import com.black.core.work.w1.TaskFlowQueue;
import com.black.core.work.w1.TaskGlobalListener;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TimerTask implements Runnable{

    private final TaskGlobalListener listener;
    private final TaskFlowQueue queue;
    private final ScheduledTaskDispatcher dispatcher;

    public TimerTask(TaskGlobalListener listener, TaskFlowQueue queue, ScheduledTaskDispatcher dispatcher) {
        this.listener = listener;
        this.queue = queue;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        //如果进行了该任务, 则表示时间点内没有执行任务, 任务没有被取消
        //回调调度器
        dispatcher.callBackDispatcher(listener, queue);
        //将任务走向下一个节点
        queue.runAgain(listener, false);
    }
}
