package com.black.core.work.w2.connect.node.instance;

import com.black.core.work.w2.connect.WorkflowInstance;
import com.black.core.work.w2.connect.node.WorkflowNode;

public interface NodeInstance {

    boolean getResult();

    boolean isInvoke();

    boolean hasBlocking();

    WorkflowNode getRelyNode();

    WorkflowInstance getInstance();

    String getInvokeTime();

    String getFinishTime();

}
