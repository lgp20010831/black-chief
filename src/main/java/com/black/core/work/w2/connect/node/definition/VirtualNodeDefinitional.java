package com.black.core.work.w2.connect.node.definition;

import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.node.VirtualNode;


public class VirtualNodeDefinitional extends AbstractNodeDefinitional{
    public VirtualNodeDefinitional(String name, String... attributeKeys) {
        super(name, VirtualNode.class, attributeKeys);
    }

    public VirtualNodeDefinitional(String name, WorkflowRunnable runnable, String... attributeKeys) {
        super(name, VirtualNode.class, runnable, attributeKeys);
        addAttributeKey(WorkflowAttributeKeys.VIRTUAL_NODE);
    }
}
