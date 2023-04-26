package com.black.core.work.w2.connect;

import com.black.core.work.w1.DefaultWorkflowBuilder;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.TimeOutHandlerWorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNode;

public class TimeOutWorkflowBuilder extends DefaultWorkflowBuilder {


    public TimeOutWorkflowBuilder(String status, String alias, WorkflowConfiguration configuration) {
        super(status, alias, configuration);
    }

    @Override
    public WorkflowNode relevance(WorkflowNode nextNode, Condition condition) {
        super.relevance(nextNode, condition);
        if (nextNode instanceof TimeOutHandlerWorkflowNode){
             doHandler(nextNode, condition);
        }
        return nextNode;
    }
}
