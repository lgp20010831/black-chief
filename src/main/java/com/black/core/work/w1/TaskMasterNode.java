package com.black.core.work.w1;

public interface TaskMasterNode<R> {

    //返回下标
    int index();

    TaskType getTaskType();

    //根据上一个节点的处理结果，选择下一个节点
    //如果是 if任务节点,那个R 就是 Boolean
    //如果是 handler block任务节点, 那个返回值可能也是 Boolean
    //如果是 swich 那么这个结果便是抽象的
    //这是一个动态的节点
    TaskMasterNode<R> flowNextNode(R result, TaskGlobalListener listener);

    //每一个节点必定有一个 next 节点
    void setNextNode(TaskMasterNode<R> nextNode);

    void setIndex(int index);

    TaskMasterNode<R> getNextNode();

    default void braking(TaskGlobalListener listener){}
}
