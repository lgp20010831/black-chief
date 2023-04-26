package com.black.core.work.w2.service;

import java.util.Map;

public interface WorkflowService {

    /** 开启一个工作流 */
    WorkflowResponse startWork(String name, Map<String, Object> formData);

    /** 取消一个任务, 并说明原因 */
    void cancelWork(String instanceId, String workflowName, String reason);

    /** 将一个工作流挂起 */
    void parkWorkflow(String name);

    /** 激活一个工作流 */
    void activitiWorkflow(String name);

    /** 节点任务失败 */
    WorkflowResponse failWork(String workflowName, String instanceId, String nodeId);

    /** 完成一个节点任务 */
    WorkflowResponse completeWork(String workflowName,String instanceId, String nodeId);

    /** 查看一个实例的状态 */
    WorkflowResponse queryInstanceState(String instanceId);

    /** 查询一个工作流的详情 */
    WorkflowResponse queryWorkflow(String name);

    WorkflowResponse queryHistoryInstance(String instanceId);
}
