package com.black.core.work.w2.connect.runnable;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.annotation.WorkflowRunnableInvoker;
import com.black.core.work.w2.connect.ill.BlockingException;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.ill.WorkflowNodeRunnableException;
import com.black.core.work.w2.connect.node.VirtualNode;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

@WorkflowRunnableInvoker
public class VirtualNodeRunnableInvoker extends AbstractNodeRunnableInvoker{
    @Override
    public boolean support(WorkflowInstanceListener listener, NodeInstance workflowNode) {
        return workflowNode.getRelyNode() instanceof VirtualNode;
    }

    @Override
    protected void doInvokeRunnable(WorkflowInstanceListener listener, NodeInstance nodeInstance) 
            throws WorkflowNodeRunnableException, BlockingException {
        DefaultNodeInstance dni = (DefaultNodeInstance) nodeInstance;
        /**
         * 要从 properties 里读取当前动态节点里所有的节点
         */
        String string = nodeInstance.getRelyNode().getString(WorkflowAttributeKeys.VIRTUAL_NODE);
        JSONObject properties = listener.getProperties();
    }
}
