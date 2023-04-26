package com.black.core.work.w2;

import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

public class F implements Condition {
    @Override
    public boolean processor(WorkflowInstanceListener ler, NodeInstance ni) throws Throwable {
        return !ni.getResult();
    }
}
