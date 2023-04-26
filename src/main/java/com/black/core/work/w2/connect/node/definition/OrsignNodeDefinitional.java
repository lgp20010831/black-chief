package com.black.core.work.w2.connect.node.definition;

import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.node.OrSignWorkflowNode;

public class OrsignNodeDefinitional extends AbstractNodeDefinitional{

    public OrsignNodeDefinitional(String name, String... attributeKeys) {
        super(name, OrSignWorkflowNode.class, attributeKeys);
    }

    public OrsignNodeDefinitional(String name, WorkflowRunnable runnable, String... attributeKeys) {
        super(name, OrSignWorkflowNode.class, runnable, attributeKeys);
        addAttributeKey(WorkflowAttributeKeys.SUB_NODES);
    }



}
