package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;


//监听者, 监听任务的进度 == 实例本身
public interface WorkflowInstanceListener {

    //获取实例对象
    WorkflowInstance getInstance();

    //获取表单属性
    JSONObject getFormData();

    WorkflowProcessing getWorkflowProcessing();

    //获取全局属性
    JSONObject getProperties();

    void setFinallyResult(boolean result);

    boolean finallyResult();

    //是否含有堵塞的节点
    boolean hasBlockingNode();

    Map<String, NodeInstance> getNodeInstances();

    NodeInstance queryNodeByNodeInstanceId(String id);

    void complete(String nodeId, boolean result);

    void refrushNodeInstances(List<NodeInstance> instances);

    List<NodeInstance> getCurrentNodeInstances();

    void setWorkflowProcessing(WorkflowProcessing workflowProcessing);

    void addAllCurrentNodes(Collection<NodeInstance> nodes);
}
