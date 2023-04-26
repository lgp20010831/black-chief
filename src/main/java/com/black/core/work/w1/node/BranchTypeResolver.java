package com.black.core.work.w1.node;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w1.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WorkflowResolver
public class BranchTypeResolver implements TaskNodeResolver<Boolean> {

    public static final String BRANCH_INDEX = "branch_index";
    private final Map<String, Integer> indexStatus = new ConcurrentHashMap<>();
    private final Map<String, List<BlockingHandlerNode>> wrapperNodes = new ConcurrentHashMap<>();

    public Integer queryIndex(String taskId){
        return indexStatus.get(taskId);
    }

    public List<BlockingHandlerNode> queryNodes(String taskId){
        return wrapperNodes.get(taskId);
    }

    public boolean reload(TaskGlobalListener listener, BranchHandlerConditionType conditionType){
        UniqueKey<?> uniqueKey = listener.getUniqueKey();
        if (uniqueKey != null){
            JSONObject jsonSource = (JSONObject) uniqueKey.getSource();
            if (jsonSource.containsKey(BRANCH_INDEX)) {
                String taskId = listener.getTaskId();
                wrapperNodes.put(taskId, conditionType.provide(listener.getGlobalParam()));
                indexStatus.put(taskId, jsonSource.getInteger(BRANCH_INDEX));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean support(TaskType taskType) {
        return taskType instanceof BranchHandlerConditionType;
    }

    @Override
    public Boolean processor(TaskType taskType, TaskGlobalListener listener, TaskFlowQueue queue) throws WorkBreakException {
        BranchHandlerConditionType conditionType = (BranchHandlerConditionType) taskType;
        JsonUniqueKey uniqueKey = (JsonUniqueKey) listener.getUniqueKey();
        String taskId = listener.getTaskId();

        //不存在
        //该 任务第一次处理
        if (!wrapperNodes.containsKey(taskId) && !indexStatus.containsKey(taskId)){
            wrapperNodes.put(taskId, conditionType.provide(listener.getGlobalParam()));
            indexStatus.put(taskId, 0);
        }else {
            indexStatus.put(taskId, indexStatus.get(taskId) + 1);
        }
        List<BlockingHandlerNode> nodes = wrapperNodes.get(taskId);
        int index = indexStatus.get(taskId);
        int size = nodes.size();

        if (index <= size -1){
            BlockingHandlerNode blockingHandlerNode = nodes.get(index);
            BlockingHandlerConditionType handlerConditionType = (BlockingHandlerConditionType) blockingHandlerNode.getTaskType();
            UniqueKey<JSONObject> key = handlerConditionType.handler(listener.getGlobalParam());
            if (uniqueKey != null){
                uniqueKey.addAllUniqueKey(key);
            }else {
                uniqueKey = (JsonUniqueKey) key;
                listener.setUniqueKey(uniqueKey);
            }
            if (uniqueKey != null){
                uniqueKey.getSource().put(BRANCH_INDEX, index);
            }
            if (blockingHandlerNode.isDelayed()) {
                queue.submitTimerTask(listener, blockingHandlerNode.getUnit(), blockingHandlerNode.getDeadline());
            }
        }
        throw new WorkBreakException(uniqueKey);
    }
}
