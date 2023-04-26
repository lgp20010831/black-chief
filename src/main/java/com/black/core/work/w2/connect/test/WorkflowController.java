package com.black.core.work.w2.connect.test;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.service.ServiceHolder;
import com.black.core.work.w2.service.WorkflowResponse;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class WorkflowController {

    @PostMapping("start")
    public Object run(@RequestBody JSONObject param, @RequestParam("type") String type){
        WorkflowResponse response = ServiceHolder.getService().startWork(type, param);
        return response;
    }

    @PostMapping("complete")
    public Object complete(@RequestBody JSONObject body, @RequestParam("type") String type){
        String instanceId = body.getString("instanceId");
        String nodeId = body.getString("nodeId");
        return ServiceHolder.getService().completeWork(type, instanceId, nodeId);
    }

    @PostMapping("fail")
    public Object fail(@RequestBody JSONObject body, @RequestParam("type") String type){
        String instanceId = body.getString("instanceId");
        String nodeId = body.getString("nodeId");
        return ServiceHolder.getService().failWork(type, instanceId, nodeId);
    }

}
