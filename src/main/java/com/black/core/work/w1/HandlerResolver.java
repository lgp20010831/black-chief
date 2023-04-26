package com.black.core.work.w1;

import com.alibaba.fastjson.JSONObject;

@WorkflowResolver
public class HandlerResolver implements TaskNodeResolver<Boolean> {
    @Override
    public boolean support(TaskType taskType) {
        return taskType instanceof BlockingHandlerConditionType;
    }

    @Override
    public Boolean processor(TaskType taskType, TaskGlobalListener listener, TaskFlowQueue queue) throws WorkBreakException {
        TaskGlobalParam globalParam = listener.getGlobalParam();
        BlockingHandlerConditionType handlerConditionType = (BlockingHandlerConditionType) taskType;
        UniqueKey<JSONObject> uniqueKey = handlerConditionType.handler(globalParam);
        throw new WorkBreakException(uniqueKey);
    }
}
