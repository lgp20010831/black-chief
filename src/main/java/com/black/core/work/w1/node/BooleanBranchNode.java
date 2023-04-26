package com.black.core.work.w1.node;

import com.black.core.work.w1.*;

import java.util.List;

public class BooleanBranchNode extends AbstractTaskNode<Boolean> {


    private final BranchTypeResolver branchTypeResolver;

    public BooleanBranchNode(TaskType taskType, BranchTypeResolver branchTypeResolver) {
        super(taskType);
        this.branchTypeResolver = branchTypeResolver;
    }

    @Override
    public void braking(TaskGlobalListener listener) {
        branchTypeResolver.reload(listener, (BranchHandlerConditionType) taskType);
    }

    @Override
    public TaskMasterNode<Boolean> flowNextNode(Boolean result, TaskGlobalListener listener) {
        if (result != null){
            if (result){
                return null;
            }
        }
        String taskId = listener.getTaskId();
        List<BlockingHandlerNode> nodes = branchTypeResolver.queryNodes(taskId);
        if (nodes == null){
            //这里只可能是重启过后才会触发
            if (branchTypeResolver.reload(listener, (BranchHandlerConditionType) taskType)){
                return this;
            }else {
                return getNextNode();
            }
        }else {
            int size = nodes.size();
            Integer index = branchTypeResolver.queryIndex(taskId);
            if (index >= size -1){
                return getNextNode();
            }
        }

        return this;
    }
}
