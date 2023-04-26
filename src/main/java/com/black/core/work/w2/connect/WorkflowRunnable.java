package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.node.AbstractWorkflowNode;

public interface WorkflowRunnable {

    void run(WorkflowInstanceListener lsr, AbstractWorkflowNode wfn) throws Throwable;
}
