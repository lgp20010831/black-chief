package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.connect.ill.UnknowNodeInstanceException;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Setter
public class DefaultWorkflowInstanceListener implements WorkflowInstanceListener{
    private WorkflowInstance instance;
    private JSONObject formData;
    private JSONObject properties;
    private WorkflowProcessing workflowProcessing;
    private final Map<String, NodeInstance> nodeInstances = new HashMap<>();
    private List<NodeInstance> currentNodeInstances;
    private boolean finallyResult;

    public DefaultWorkflowInstanceListener(){
        properties = new JSONObject();
    }

    @Override
    public WorkflowInstance getInstance() {
        return instance;
    }

    @Override
    public JSONObject getFormData() {
        return formData;
    }

    @Override
    public WorkflowProcessing getWorkflowProcessing() {
        return workflowProcessing;
    }

    @Override
    public JSONObject getProperties() {
        return properties;
    }

    @Override
    public void setFinallyResult(boolean result) {
        finallyResult = result;
    }

    @Override
    public boolean finallyResult() {
        return finallyResult;
    }

    @Override
    public boolean hasBlockingNode() {
        for (NodeInstance nodeInstance : getNodeInstances().values()) {
            if(nodeInstance.hasBlocking()){
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, NodeInstance> getNodeInstances() {
        return nodeInstances;
    }

    @Override
    public NodeInstance queryNodeByNodeInstanceId(String id) {
        if (nodeInstances != null){
            for (NodeInstance nodeInstance : nodeInstances.values()) {
                if (id.equals(nodeInstance.getRelyNode().id())){
                    return nodeInstance;
                }
            }
        }
        return null;
    }

    @Override
    public void complete(String nodeId, boolean result) {
        NodeInstance nodeInstance = queryNodeByNodeInstanceId(nodeId);
        if (nodeInstance == null){
            throw new UnknowNodeInstanceException("无法找到节点实例: " + nodeId);
        }
        boolean current = false;
        for (NodeInstance currentNodeInstance : getCurrentNodeInstances()) {
         if (nodeInstance.getRelyNode().id().equals(currentNodeInstance.getRelyNode().id())){
             current = true;
             break;
         }
        }

        if (!current){
            throw new RuntimeException("你无法完成或失败一个没有进行的节点: " + nodeId);
        }

        if (nodeInstance instanceof DefaultNodeInstance){
            DefaultNodeInstance defaultNodeInstance = (DefaultNodeInstance) nodeInstance;
            defaultNodeInstance.setResult(result);
        }
    }

    @Override
    public void refrushNodeInstances(List<NodeInstance> instances) {
        for (NodeInstance nodeInstance : instances) {
            nodeInstances.put(nodeInstance.getRelyNode().name(), nodeInstance);
        }
    }

    @Override
    public List<NodeInstance> getCurrentNodeInstances() {
        if (currentNodeInstances == null){
            List<String> invokeNodes = instance.getCurrentInvokeNodes();
            currentNodeInstances = new ArrayList<>();
            if (invokeNodes != null){
                for (String alias : invokeNodes) {
                    NodeInstance instance = getNodeInstances().get(alias);
                    if (instance != null){
                        currentNodeInstances.add(instance);
                    }
                }
            }else {
                List<String> currentAlias = new ArrayList<>();
                for (NodeInstance nodeInstance : getNodeInstances().values()) {
                    if (nodeInstance.getRelyNode().isHead()){
                        currentNodeInstances.add(nodeInstance);
                        currentAlias.add(nodeInstance.getRelyNode().name());
                    }
                }
                instance.setCurrentInvokeNodes(currentAlias);
            }
        }
        return currentNodeInstances;
    }

    @Override
    public void addAllCurrentNodes(Collection<NodeInstance> nodes) {
        if (currentNodeInstances != null){
            currentNodeInstances.clear();
            currentNodeInstances.addAll(nodes);
            instance.setCurrentInvokeNodes(nodes.stream().map(ni -> ni.getRelyNode().name()).collect(Collectors.toList()));
        }
    }


}
