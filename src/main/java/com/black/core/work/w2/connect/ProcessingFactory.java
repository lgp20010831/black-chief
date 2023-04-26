package com.black.core.work.w2.connect;

public class ProcessingFactory {
    public static WorkflowProcessing createProcessing(WorkflowDefinitional definitional,
                                                      WorkflowRefinedDispatcher refinedDispatcher){
        DefaultWorkflowProcessing workflowProcessing = new DefaultWorkflowProcessing(definitional);
        workflowProcessing.setDispatcher(refinedDispatcher);
        workflowProcessing.setConfiguration(refinedDispatcher.getWorkflowConfiguration());
        return workflowProcessing;
    }
}
