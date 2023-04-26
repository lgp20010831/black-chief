package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface WorkflowInstance {

    //实例 id
    String id();

    //工作流模板 id
    String workflowId();

    //获取已经走过的流程
    List<String> getRoutePath();

    void addRoutePath(ConnectRouteWraper routeWraper);

    List<String> getCurrentInvokeNodes();

    void addCurrentNode(String nodeAlias);

    void setCurrentInvokeNodes(List<String> nodes);

    //返回实例创建时间
    String createTime();

    //设置创建时间
    void setCreateTime(String createTime);

    //更新时间
    String updateTime();

    void setUpdateTime(String updateTime);

    JSONObject getScheduledTime();

    void putScheduledTime(String nodeId, Long time);

    void putAllScheduledTime(JSONObject source);

    void removeScheduledTime(String nodeId);
}
