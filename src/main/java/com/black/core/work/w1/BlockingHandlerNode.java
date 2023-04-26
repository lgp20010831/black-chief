package com.black.core.work.w1;

import java.util.concurrent.TimeUnit;

public class BlockingHandlerNode extends AbstractTaskNode<Boolean> implements TimeOutNode {

    private final TimeUnit timeUnit;
    private long time = -1;
    private boolean delayed = false;

    public BlockingHandlerNode(TaskType taskType){
        this(taskType, null, -1);
    }

    public BlockingHandlerNode(TaskType taskType, TimeUnit timeUnit, long time) {
        super(taskType);
        this.time = time;
        this.timeUnit = timeUnit;
        if (timeUnit != null && time != -1){
            delayed = true;
        }
    }

    @Override
    public TaskMasterNode<Boolean> flowNextNode(Boolean result, TaskGlobalListener listener) {
        if (result != null){
            if (!result){
                return nextNode;
            }
        }
        return null;
    }

    @Override
    public boolean isDelayed() {
        return delayed;
    }

    @Override
    public TimeUnit getUnit() {
        return timeUnit;
    }

    @Override
    public long getDeadline() {
        return time;
    }
}
