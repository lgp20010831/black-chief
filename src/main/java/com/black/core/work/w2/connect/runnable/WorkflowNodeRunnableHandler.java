package com.black.core.work.w2.connect.runnable;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.ill.BlockingException;
import com.black.core.work.w2.connect.ill.WorkflowNodeRunnableException;

import java.util.concurrent.locks.ReentrantLock;

public interface WorkflowNodeRunnableHandler {

    ReentrantLock lock = new ReentrantLock();

    boolean support(WorkflowInstanceListener listener, NodeInstance workflowNode);

    void invokeRunnable(WorkflowInstanceListener listener, NodeInstance workflowNode) throws BlockingException, WorkflowNodeRunnableException;
}
