package com.black.core.work.w1;


public class IfConditionNode extends AbstractTaskNode<Boolean> {

    //left 作为 true
    TaskMasterNode<Boolean> left;

    //right 作为 false
    TaskMasterNode<Boolean> right;

    TaskMasterNode<Boolean> father;

    public IfConditionNode(TaskType taskType) {
        super(taskType);
    }


    public void setFather(TaskMasterNode<Boolean> father) {
        this.father = father;
    }

    public TaskMasterNode<Boolean> getFather() {
        return father;
    }

    @Override
    public TaskMasterNode<Boolean> flowNextNode(Boolean result, TaskGlobalListener listener) {
        if (result != null){
            if (result){
                return left;
            }else {
                return right;
            }
        }
        return null;
    }


    public void setLeft(TaskMasterNode<Boolean> left) {
        this.left = left;
        this.left.setIndex(index() + 1);
    }


    public void setRight(TaskMasterNode<Boolean> right) {
        this.right = right;
        this.right.setIndex(index() + 1);
    }
}
