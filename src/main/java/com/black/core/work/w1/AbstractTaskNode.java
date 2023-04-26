package com.black.core.work.w1;

public abstract class AbstractTaskNode<R> implements TaskMasterNode<R> {

    protected int index;

    protected final TaskType taskType;

    protected TaskMasterNode<R> nextNode;

    protected AbstractTaskNode(TaskType taskType) {
        this.taskType = taskType;
    }


    @Override
    public int index() {
        return index;
    }

    @Override
    public TaskType getTaskType() {
        return taskType;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }


    public void setNextNode(TaskMasterNode<R> nextNode) {
        this.nextNode = nextNode;
    }

    @Override
    public TaskMasterNode<R> getNextNode() {
        return nextNode;
    }
}
