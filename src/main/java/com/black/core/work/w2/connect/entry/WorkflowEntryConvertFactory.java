package com.black.core.work.w2.connect.entry;

import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.utils.WorkUtils;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.Map;

public class WorkflowEntryConvertFactory {

    public static WorkflowInstanceEntry convertInstanceEntry(WorkflowInstanceListener listener){
        if (listener == null){
            return null;
        }
        WorkflowInstance instance = listener.getInstance();
        WorkflowInstanceEntry instanceEntry = WorkUtils.parse(instance, WorkflowInstanceEntry.class);
        return WorkUtils.load(listener, instanceEntry);
    }

    public static WorkflowNodeInstanceEntry paserNode(WorkflowNode node, WorkflowDefinitional workflowDefinitional){
        WorkflowNodeInstanceEntry entry = WorkUtils.parse(node, WorkflowNodeInstanceEntry.class);
        entry.setId(WorkUtils.getRandomId());
        WorkflowNodeDefinitional definitional = node.getDefinitional();
        entry.setModuleId(definitional.id());
        entry.setModuleName(definitional.name());
        entry.setWorkflowId(workflowDefinitional.getId());
        entry.setWorkflowName(workflowDefinitional.getAlias());
        entry.setAttributes(node.attributes().toString());
        return entry;
    }

    public static WorkflowInstance convertInstance(WorkflowInstanceEntry instanceEntry){
        if (instanceEntry == null){
            return null;
        }
        return WorkUtils.parse(instanceEntry, DefaultWorkflowInstance.class);
    }

    public static WorkflowInstanceListener convertInstanceListener(WorkflowInstanceEntry instanceEntry){
        if (instanceEntry == null){
            return null;
        }
        DefaultWorkflowInstanceListener listener = WorkUtils.parse(instanceEntry, DefaultWorkflowInstanceListener.class);
        WorkflowInstance instance = convertInstance(instanceEntry);
        listener.setInstance(instance);
        return listener;
    }

    public static WorkflowRouteEntry convertRouteEntry(ConnectRouteWraper routeWraper,
                                                       Map<String, WorkflowNode> nodeEntries){
        WorkflowRouteEntry routeEntry = WorkUtils.parse(routeWraper, WorkflowRouteEntry.class);
        WorkflowNode startNode = nodeEntries.get(routeWraper.getStartAlias());
        if (startNode == null){
            throw new RuntimeException("can not find node: " + routeWraper.getStartAlias());
        }
        routeEntry.setStartNodeId(startNode.id());
        WorkflowNode endNode = nodeEntries.get(routeWraper.getEndAlias());
        if (endNode == null){
            throw new RuntimeException("can not find node: " + routeWraper.getEndAlias());
        }
        routeEntry.setEndNodeId(endNode.id());
        return routeEntry;
    }
}
