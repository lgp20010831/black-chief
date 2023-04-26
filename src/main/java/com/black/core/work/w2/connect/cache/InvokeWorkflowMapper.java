package com.black.core.work.w2.connect.cache;

import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.entry.WorkflowNodeInstanceDetailsEntry;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.List;

//提供真正操作 workflowMapper 接口
public interface InvokeWorkflowMapper {

    //write
    String writeEngine(WorkflowDefinitional workflowDefinitional, boolean update);

    String writeNode(WorkflowNode workflowNode, WorkflowDefinitional definitional);

    String writeRoute(ConnectRouteWraper routeWraper, WorkflowDefinitional definitional);

    String writeInstance(WorkflowInstanceListener instanceListener);

    String writeNodeModule(WorkflowNodeDefinitional definitional);

    //read
    WorkflowInstance getInstance(String workflowId, String instanceId);

    WorkflowInstanceListener getListener(String instanceId, WorkflowProcessing workflowProcessing);

    List<WorkflowNodeInstanceDetailsEntry> readDetailsEntry(String instanceId);

    //update
    void updateInstance(WorkflowInstanceListener listener);

    void updateNodeModule(WorkflowNodeDefinitional definitional);

    //remove
    void removeNode(String alias);
}
