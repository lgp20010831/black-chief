package com.black.core.work.w1.node;

import com.black.core.work.w1.BlockingHandlerNode;
import com.black.core.work.w1.TaskGlobalParam;
import com.black.core.work.w1.TaskType;

import java.util.List;

public interface BranchHandlerConditionType extends TaskType {

    @Override
    default boolean isBlocking(){
        return true;
    }

    @Override
    default boolean isServerBraking(){
        return true;
    }

    //提供分支
    List<BlockingHandlerNode> provide(TaskGlobalParam globalParam);
}
