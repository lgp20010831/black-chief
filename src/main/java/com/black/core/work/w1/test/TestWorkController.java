package com.black.core.work.w1.test;

import com.alibaba.fastjson.JSONObject;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.work.w1.WorkFlowScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin
@GlobalEnhanceRestController
public class TestWorkController {

    @Autowired(required = false)
    WorkFlowScheduler workFlowScheduler;

    @PostMapping("invokeYcsb")
    public Object invokeYcsb(@RequestBody JSONObject json){
        String type = HttpRequestUtil.singtonRetrieval(json, "type");
        return workFlowScheduler.createInstance(type, json);
    }

    @GetMapping("taskComplete")
    public Object complete(String id, Boolean r, String type){
        return workFlowScheduler.completeInstance(type, id, r);
    }

}
