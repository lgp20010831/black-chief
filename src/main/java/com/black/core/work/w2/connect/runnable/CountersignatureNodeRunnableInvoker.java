package com.black.core.work.w2.connect.runnable;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.ill.BlockingException;
import com.black.core.work.w2.connect.ill.WorkflowNodeRunnableException;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.annotation.WorkflowRunnableInvoker;
import com.black.core.work.w2.connect.node.CountersignatureWorkflowNode;

@WorkflowRunnableInvoker
public class CountersignatureNodeRunnableInvoker extends AbstractNodeRunnableInvoker {
    @Override
    public boolean support(WorkflowInstanceListener listener, NodeInstance workflowNode) {
        return workflowNode.getRelyNode() instanceof CountersignatureWorkflowNode;
    }

    @Override
    protected void doInvokeRunnable(WorkflowInstanceListener listener, NodeInstance nodeInstance) throws WorkflowNodeRunnableException, BlockingException {
        super.doInvokeRunnable(listener, nodeInstance);
        if (nodeInstance instanceof DefaultNodeInstance) {
            ((DefaultNodeInstance) nodeInstance).setResult(true);
        }
    }
}
