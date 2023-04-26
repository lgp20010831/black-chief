package com.black.core.work.w2.connect.node.definition;

import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.node.CountersignatureWorkflowNode;

public class CountersignatureNodeDefinitional extends AbstractNodeDefinitional {


    public CountersignatureNodeDefinitional(String name, String... attributeKeys) {
        super(name, CountersignatureWorkflowNode.class, attributeKeys);
    }

    public CountersignatureNodeDefinitional(String name, WorkflowRunnable runnable, String... attributeKeys) {
        super(name, CountersignatureWorkflowNode.class, runnable, attributeKeys);
        addAttributeKey(WorkflowAttributeKeys.SUB_NODES);
    }
}
