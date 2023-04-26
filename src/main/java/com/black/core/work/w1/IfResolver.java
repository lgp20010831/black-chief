package com.black.core.work.w1;

@WorkflowResolver
public class IfResolver implements TaskNodeResolver<Boolean> {
    @Override
    public boolean support(TaskType taskType) {
        return taskType instanceof IfConditionType;
    }

    @Override
    public Boolean processor(TaskType taskType, TaskGlobalListener listener, TaskFlowQueue queue) throws WorkBreakException {
        TaskGlobalParam globalParam = listener.getGlobalParam();
        IfConditionType ifConditionType = (IfConditionType) taskType;
        return ifConditionType.judge(globalParam);
    }
}
