package com.black.core.work.w2.connect;

import com.black.core.builder.Col;
import com.black.core.work.w2.connect.ill.UnknowNodeDefinitionalException;
import com.black.core.work.w2.connect.node.NodeFactory;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultWorkflowDrawer implements WorkflowDrawer{

    private final Map<String, WorkflowNodeDefinitional> nodeDefinitionalMap;
    private final WorkflowBuilder builder;
    private final String workflowName;
    protected AtomicInteger level = new AtomicInteger(0);

    public DefaultWorkflowDrawer(Map<String, WorkflowNodeDefinitional> nodeDefinitionalMap, WorkflowBuilder builder, String workflowName) {
        this.nodeDefinitionalMap = nodeDefinitionalMap;
        this.builder = builder;
        this.workflowName = workflowName;
    }

    @Override
    public WorkflowNode put(String moduleName, String nodeName, Map<String, Object> attributeFormData, Condition condition) {
        WorkflowNode workflowNode = createNode(moduleName, nodeName, attributeFormData);
        level.incrementAndGet();
        workflowNode.setIndex(level.get());
        builder.relevance(workflowNode, condition);
        return workflowNode;
    }

    @Override
    public WorkflowNode put(String moduleName, String nodeName, Condition condition) {
        return put(moduleName, nodeName, new HashMap<>(), condition);
    }

    @Override
    public WorkflowNode put(String moduleName, Map<String, Object> attributeFormData, Condition condition) {
        return put(moduleName, NodeFactory.createDefaultName(moduleName, workflowName), attributeFormData, condition);
    }

    @Override
    public WorkflowNode put(String moduleName, Condition condition) {
        return put(moduleName, new HashMap<>(), condition);
    }


    @Override
    public WorkflowNode createNode(String moduleName, String nodeName, Map<String, Object> attributeFormData) {
        WorkflowNodeDefinitional nodeDefinitional = nodeDefinitionalMap.get(moduleName);
        if (nodeDefinitional == null){
            throw new UnknowNodeDefinitionalException("无法找到节点定义: " + moduleName);
        }
        return NodeFactory.create(nodeDefinitional, Col.ar(nodeName, nodeDefinitional, attributeFormData));
    }

    @Override
    public WorkflowNode createNode(String moduleName, Map<String, Object> attributeFormData) {
        return createNode(moduleName, NodeFactory.createDefaultName(moduleName, workflowName), attributeFormData);
    }

    @Override
    public void refrushIndex(int index) {
        level.set(index);
    }
}
