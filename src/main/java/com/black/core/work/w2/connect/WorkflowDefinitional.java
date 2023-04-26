package com.black.core.work.w2.connect;

import com.black.core.json.NotNull;
import com.black.core.work.w2.connect.node.WorkflowNode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class WorkflowDefinitional {

    String id;
    String status;
    String alias;
    WorkflowProcessing engine;

    //所有节点节点
    List<WorkflowNode> nodeAliasSet;

    Map<String, WorkflowNode> nodeMap;

    //所有的连线
    Set<ConnectRouteWraper> routeWrapers;

    public WorkflowDefinitional(String status, String alias) {
        this.status = status;
        this.alias = alias;
        nodeAliasSet = new ArrayList<>();
        routeWrapers = new HashSet<>();
    }

    public void setId(String id) {
        this.id = id;
        engine.setId(id);
        for (ConnectRouteWraper routeWraper : routeWrapers) {
            routeWraper.setWorkflowId(id);
        }
    }

    public void end(){
        if (!nodeAliasSet.isEmpty()) {
            WorkflowNode node = nodeAliasSet.get(0);
            node.setHead(true);
            WorkflowNode workflowNode = nodeAliasSet.get(nodeAliasSet.size() - 1);
            workflowNode.setTail(true);
        }
    }

    public void setEngine(WorkflowProcessing engine) {
        this.engine = engine;
    }


    public void addConnect(ConnectRouteWraper routeWraper){
        routeWrapers.add(routeWraper);
    }

    public void addNode(WorkflowNode workflowNode){
        nodeAliasSet.add(workflowNode);
    }

    public void removeConnect(@NotNull String startNodeAlias, @NotNull String endNodeAlias){
        routeWrapers.removeIf(rw ->{
            return startNodeAlias.equals(rw.startAlias) && endNodeAlias.equals(rw.endAlias);
        });
    }

    public Map<String, WorkflowNode> getNodeMap() {
        if (nodeMap == null){
            nodeMap = new HashMap<>();
            for (WorkflowNode workflowNode : nodeAliasSet) {
                nodeMap.put(workflowNode.name(), workflowNode);
            }
        }
        return nodeMap;
    }
}
