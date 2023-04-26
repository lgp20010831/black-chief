package com.black.core.work.w2.action;


import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.service.ServiceHolder;
import com.black.core.work.w2.service.WorkflowResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("all")
public abstract class AbstractWorkflowController {


    @PostMapping("start")
    @ApiOperation("启动一个工作流模板, 生成一个实例")
    public WorkflowResponse run(@RequestBody @ApiParam("表单属性") JSONObject param,
                                @RequestParam("name") @ApiParam("工作流模板名称") String name){
        WorkflowResponse response = ServiceHolder.getService().startWork(name, param);
        return response;
    }

    @GetMapping("complete")
    @ApiOperation("完成一个工作流的节点")
    public WorkflowResponse complete(@RequestParam @ApiParam("工作流实例id") String instanceId,
                           @RequestParam @ApiParam("完成的工作流节点id") String nodeId,
                           @RequestParam @ApiParam("工作流模板名称") String name){
        return ServiceHolder.getService().completeWork(name, instanceId, nodeId);
    }

    @GetMapping("fail")
    @ApiOperation("失败一个工作流的节点")
    public WorkflowResponse fail(@RequestParam @ApiParam("工作流实例id") String instanceId,
                       @RequestParam @ApiParam("完成的工作流节点id") String nodeId,
                       @RequestParam @ApiParam("工作流模板名称") String name){
        return ServiceHolder.getService().failWork(name, instanceId, nodeId);
    }

    @GetMapping("queryInstanceState")
    @ApiOperation("查看一个实例的状态")
    public WorkflowResponse queryInstanceState(@RequestParam @ApiParam("工作流实例id") String instanceId){
        return ServiceHolder.getService().queryInstanceState(instanceId);
    }

    @GetMapping("queryWorkflow")
    @ApiOperation("查询一个工作流模板信息")
    public WorkflowResponse queryWorkflow(@RequestParam @ApiParam("工作流模板名称") String name){
        return ServiceHolder.getService().queryWorkflow(name);
    }

    @GetMapping("parkWorkflow")
    @ApiOperation("将一个工作流模板挂起")
    public void parkWorkflow(@RequestParam @ApiParam("工作流模板名称") String name){
        ServiceHolder.getService().parkWorkflow(name);
    }

    @GetMapping("activitiWorkflow")
    @ApiOperation("将一个工作流模板激活")
    public void activitiWorkflow(@RequestParam @ApiParam("工作流模板名称") String name){
        ServiceHolder.getService().activitiWorkflow(name);
    }

    @GetMapping("cancelWork")
    @ApiOperation("取消一个工作流实例")
    public void cancelWork(@RequestParam @ApiParam("工作流实例id") String instanceId,
                           @RequestParam @ApiParam("工作流模板名称") String name,
                           @ApiParam("取消原因") String reason){
        ServiceHolder.getService().cancelWork(instanceId, name, reason);
    }

    @GetMapping("queryHistoryInstance")
    @ApiOperation("根据实例id查询历史实例数据")
    public WorkflowResponse queryHistoryInstance(@RequestParam @ApiParam("已经完成的工作流实例id") String instanceId){
        return ServiceHolder.getService().queryHistoryInstance(instanceId);
    }
}
