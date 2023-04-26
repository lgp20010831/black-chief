package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.node.instance.NodeInstance;

public interface Condition {


    boolean processor(WorkflowInstanceListener ler, NodeInstance ni) throws Throwable;

}
