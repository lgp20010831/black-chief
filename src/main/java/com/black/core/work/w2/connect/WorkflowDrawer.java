package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.node.WorkflowNode;

import java.util.Map;

public interface WorkflowDrawer {

    WorkflowNode put(String moduleName, String nodeName, Map<String, Object> attributeFormData, Condition condition);

    WorkflowNode put(String moduleName, String nodeName, Condition condition);

    WorkflowNode put(String moduleName, Map<String, Object> attributeFormData, Condition condition);

    WorkflowNode put(String moduleName, Condition condition);

    WorkflowNode createNode(String moduleName, String nodeName, Map<String, Object> attributeFormData);

    WorkflowNode createNode(String moduleName, Map<String, Object> attributeFormData);

    void refrushIndex(int index);
}
