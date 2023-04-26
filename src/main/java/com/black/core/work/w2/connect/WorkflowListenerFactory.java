package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.utils.WorkUtils;

public class WorkflowListenerFactory {


    public static WorkflowInstanceListener createListener(JSONObject formData, WorkflowProcessing workflowProcessing){
        DefaultWorkflowInstanceListener listener = new DefaultWorkflowInstanceListener();
        listener.setFormData(formData);
        listener.setWorkflowProcessing(workflowProcessing);

        //create instance
        DefaultWorkflowInstance defaultWorkflowInstance = new DefaultWorkflowInstance();
        defaultWorkflowInstance.setId(WorkUtils.getRandomId());
        defaultWorkflowInstance.setWorkflowId(workflowProcessing.id());
        defaultWorkflowInstance.setCreateTime(WorkUtils.getTime());
        defaultWorkflowInstance.setUpdateTime(WorkUtils.getTime());

        listener.setInstance(defaultWorkflowInstance);
        return listener;
    }

}
