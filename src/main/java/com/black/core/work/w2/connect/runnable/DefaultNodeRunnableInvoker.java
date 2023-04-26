package com.black.core.work.w2.connect.runnable;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.annotation.WorkflowRunnableInvoker;
import com.black.core.work.w2.connect.node.DefaultHandlerWorkflowNode;

@WorkflowRunnableInvoker
public class DefaultNodeRunnableInvoker extends AbstractNodeRunnableInvoker{
    @Override
    public boolean support(WorkflowInstanceListener listener, NodeInstance workflowNode) {
        return workflowNode.getRelyNode() instanceof DefaultHandlerWorkflowNode;
    }
}
