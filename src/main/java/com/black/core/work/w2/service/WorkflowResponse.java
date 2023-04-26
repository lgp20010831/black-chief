package com.black.core.work.w2.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.core.json.Trust;
import com.black.core.util.StreamUtils;
import com.black.core.work.w2.connect.WorkflowInstance;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

//响应类
@Trust
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WorkflowResponse {

    private Object data;

    //实例 id
    @ApiModelProperty("工作流实例id")
    private String instanceId;

    //工作流 id
    @ApiModelProperty("工作流模板名称")
    private String workflowName;

    @ApiModelProperty("当前正在执行的节点实例id")
    private JSONArray currentNodeIds = new JSONArray();

    //当前正在进行的节点 id
    @ApiModelProperty("当前正在执行的节点实例属性")
    private Collection<JSONObject> currentNodeInfos;

    @ApiModelProperty("当前工作流模板所持有的所有节点实例属性")
    private Collection<JSONObject> nodeInfos;

    public WorkflowResponse(Object data, String instanceId, String workflowName, Collection<JSONObject> currentNodeInfos, Collection<JSONObject> nodeInfos) {
        this.data = data;
        this.instanceId = instanceId;
        this.workflowName = workflowName;
        this.currentNodeInfos = currentNodeInfos;
        this.nodeInfos = nodeInfos;
        for (JSONObject currentNodeInfo : currentNodeInfos) {
            JSONObject relyNode = currentNodeInfo.getJSONObject("relyNode");
            if (relyNode != null){
                currentNodeIds.add(relyNode.getString("id"));
            }
        }
    }

    public WorkflowResponse(Object data) {
        this.data = data;
    }

    public static WorkflowResponse convert(WorkflowInstanceListener listener){
        //所有的节点
        Map<String, NodeInstance> nodeInstances = listener.getNodeInstances();
        //当前执行的节点
        List<NodeInstance> currentNodeInstances = listener.getCurrentNodeInstances();

        WorkflowInstance instance = listener.getInstance();
        String instanceId = instance.id();
        String alias = listener.getWorkflowProcessing().alias();
        return new WorkflowResponse(null, instanceId, alias,
                StreamUtils.mapList(currentNodeInstances, ni ->{
                    return JsonUtils.toJson(ni,  true, true, Object.class);
                }),
                StreamUtils.mapList(nodeInstances.values(), ni ->{
                    return JsonUtils.toJson(ni,  true, true, Object.class);
                }));
    }
}
