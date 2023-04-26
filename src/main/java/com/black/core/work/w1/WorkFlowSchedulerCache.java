package com.black.core.work.w1;

import com.black.core.work.w2.connect.WorkflowRefinedDispatcher;

public class WorkFlowSchedulerCache {

    static WorkFlowScheduler workFlowScheduler;
    static WorkflowRefinedDispatcher workflowRefinedDispatcher;
    public static WorkFlowScheduler getWorkFlowScheduler() {
        return workFlowScheduler;
    }

    public static void setWorkflowRefinedDispatcher(WorkflowRefinedDispatcher workflowRefinedDispatcher) {
        WorkFlowSchedulerCache.workflowRefinedDispatcher = workflowRefinedDispatcher;
    }

    public static WorkflowRefinedDispatcher getWorkflowRefinedDispatcher() {
        return workflowRefinedDispatcher;
    }
}
