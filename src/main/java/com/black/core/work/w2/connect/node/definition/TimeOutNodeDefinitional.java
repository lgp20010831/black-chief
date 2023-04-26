package com.black.core.work.w2.connect.node.definition;

import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.node.TimeOutHandlerWorkflowNode;

public class TimeOutNodeDefinitional extends AbstractNodeDefinitional {


    public TimeOutNodeDefinitional(String name, String... attributeKeys) {
        this(name, null,  attributeKeys);
    }

    public TimeOutNodeDefinitional(String name, WorkflowRunnable runnable, String... attributeKeys) {
        super(name, TimeOutHandlerWorkflowNode.class, runnable, attributeKeys);
        addAttributeKey(WorkflowAttributeKeys.TIME_OUT_TIME).addAttributeKey(WorkflowAttributeKeys.TIME_OUT_UNIT);
    }
}
