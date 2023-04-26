package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.Trust;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Trust
public class DefaultWorkflowInstance implements WorkflowInstance{

    String id;
    String workflowId;
    List<String> routePath = new ArrayList<>();
    List<String> currentNodeName;
    String createTime;
    String updateTime;

    JSONObject nodeScheduledTime = new JSONObject();

    @Override
    public String id() {
        return id;
    }

    @Override
    public String workflowId() {
        return workflowId;
    }

    @Override
    public List<String> getRoutePath() {
        return routePath;
    }

    @Override
    public void addRoutePath(ConnectRouteWraper routeWraper) {
        routePath.add(routeWraper.getName());
    }

    @Override
    public List<String> getCurrentInvokeNodes() {
        return currentNodeName;
    }

    @Override
    public void addCurrentNode(String nodeAlias) {
        currentNodeName.add(nodeAlias);
    }

    @Override
    public void setCurrentInvokeNodes(List<String> nodes) {
        this.currentNodeName = nodes;
    }

    @Override
    public String createTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String updateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public JSONObject getScheduledTime() {
        return nodeScheduledTime;
    }

    @Override
    public void putScheduledTime(String nodeId, Long time) {
        nodeScheduledTime.put(nodeId, time);
    }

    @Override
    public void putAllScheduledTime(JSONObject source) {
        nodeScheduledTime.putAll(source);
    }

    @Override
    public void removeScheduledTime(String nodeId) {
        nodeScheduledTime.remove(nodeId);
    }
}
