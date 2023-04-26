package com.black.core.work.w2.service;

import com.black.core.work.w2.connect.WorkflowDefinitional;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;
import com.black.core.work.w2.connect.WorkflowStatus;
import com.black.core.work.w2.connect.cache.SqlServiceWriter;
import com.black.core.work.w2.connect.cache.SqlWritorProxy;
import com.black.core.work.w2.connect.cache.WorkflowMapMapper;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.entry.WorkflowEntry;
import com.black.core.work.w2.connect.entry.WorkflowEntryConvertFactory;
import com.black.core.work.w2.connect.entry.WorkflowInstanceEntry;
import com.black.core.work.w2.connect.ill.UnknowWorkflowException;
import com.black.core.work.w2.connect.node.NodeFactory;

import java.util.Map;

public class DefaultWorkflowServiceImpl implements WorkflowService {

    private final WorkflowConfiguration configuration;

    public DefaultWorkflowServiceImpl(WorkflowConfiguration configuration) {
        this.configuration = configuration;
        configuration.setWorkflowServicce(this);
    }

    @Override
    public WorkflowResponse startWork(String name, Map<String, Object> formData) {
        WorkflowDefinitional definitional = configuration.getDispatcher().getWorkflowDefinitionalCache().get(name);
        if (definitional == null){
            throw new UnknowWorkflowException("无法找到工作流模板 :" + name);
        }
        if (definitional.getStatus().equals(WorkflowStatus.HANG)) {
            throw new RuntimeException("该工作流模板已经挂起了: " + name);
        }
        WorkflowInstanceListener listener = definitional.getEngine().run(formData);
        return WorkflowResponse.convert(listener);
    }

    @Override
    public void cancelWork(String instanceId, String workflowName, String reason) {
        configuration.getDispatcher().cancelWork(instanceId, workflowName);
    }

    @Override
    public void parkWorkflow(String name) {
        configuration.getDispatcher().parkWorkflow(name);
    }

    @Override
    public void activitiWorkflow(String name) {
        configuration.getDispatcher().activitiWorkflow(name);
    }

    @Override
    public WorkflowResponse failWork(String workflowName, String instanceId, String nodeId) {
        WorkflowDefinitional definitional = configuration.getDispatcher().getWorkflowDefinitionalCache().get(workflowName);
        if (definitional == null){
            throw new UnknowWorkflowException("无法找到工作流模板 :" + workflowName);
        }
        WorkflowInstanceListener listener = definitional.getEngine().complete(instanceId, nodeId, false);
        return WorkflowResponse.convert(listener);
    }

    @Override
    public WorkflowResponse completeWork(String workflowName,String instanceId, String nodeId) {
        WorkflowDefinitional definitional = configuration.getDispatcher().getWorkflowDefinitionalCache().get(workflowName);
        if (definitional == null){
            throw new UnknowWorkflowException("无法找到工作流模板 :" + workflowName);
        }
        WorkflowInstanceListener listener = definitional.getEngine().complete(instanceId, nodeId, true);
        return WorkflowResponse.convert(listener);
    }

    @Override
    public WorkflowResponse queryInstanceState(String instanceId) {
        WorkflowInstanceEntry instanceEntry = configuration.getDatabaseCache().getActivityInstanceCache().get(instanceId);
        if (instanceEntry == null){
            throw new RuntimeException("实例不存在, id: " + instanceId);
        }
        WorkflowProcessing processing = configuration.getDispatcher().queryProcessingById(instanceEntry.getWorkflowId());
        if (processing == null){
            throw new RuntimeException("找不到该工作流");
        }
        WorkflowInstanceListener listener = WorkflowEntryConvertFactory.convertInstanceListener(instanceEntry);
        listener.setWorkflowProcessing(processing);
        listener.refrushNodeInstances(NodeFactory.instanceNodeQueue(processing.getNodeQueue(), listener));
        return WorkflowResponse.convert(listener);
    }

    @Override
    public WorkflowResponse queryWorkflow(String name) {
        WorkflowEntry workflowEntry = configuration.getDatabaseCache().getWorkflowEntryCache().get(name);
        return new WorkflowResponse(workflowEntry);
    }

    @Override
    public WorkflowResponse queryHistoryInstance(String instanceId) {
        SqlServiceWriter serviceWriter = (SqlServiceWriter) configuration.getDatabaseCache();
        SqlWritorProxy proxy = serviceWriter.getProxy();
        WorkflowMapMapper mapsqlMapper = proxy.getMapsqlMapper();
        Map<String, Object> historyInstance = mapsqlMapper.findById("workflow_history_instance", instanceId);
        return new WorkflowResponse(historyInstance);
    }
}
