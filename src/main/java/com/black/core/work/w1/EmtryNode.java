package com.black.core.work.w1;

public class EmtryNode extends AbstractTaskNode<Boolean> {

    protected EmtryNode(TaskType taskType) {
        super(taskType);
    }

    @Override
    public TaskMasterNode flowNextNode(Boolean result, TaskGlobalListener listener) {
        return null;
    }
}
