package com.black.core.work.w2.connect.node.definition;

import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.node.DefaultHandlerWorkflowNode;

public class DefaultNodeDefinitional extends AbstractNodeDefinitional {


    public DefaultNodeDefinitional(String name, String... attributeKeys) {
        super(name, DefaultHandlerWorkflowNode.class, attributeKeys);
    }

    public DefaultNodeDefinitional(String name, WorkflowRunnable runnable, String... attributeKeys) {
        super(name, DefaultHandlerWorkflowNode.class, runnable, attributeKeys);
    }
}
