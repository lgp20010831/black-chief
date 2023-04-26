package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.WorkflowNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface WorkflowProcessing {

    void init(WorkflowDefinitional definitional, WorkflowRefinedDispatcher dispatcher);

    String id();

    void setId(String id);

    String alias();

    List<WorkflowNode> getNodeQueue();

    WorkflowDefinitional getDefinitional();

    WorkflowConfiguration getConfiguration();

    void submitTimerTask(WorkflowInstanceListener listener, String nodeId, TimeUnit unit, long life);

    WorkflowInstanceListener complete(String taskId, String nodeId, boolean result);

    WorkflowInstanceListener run(Map<String, Object> param);

    WorkflowInstanceListener run(JSONObject param);

    void cancelWork(String instanceId);

    int size();

    void close();
}
