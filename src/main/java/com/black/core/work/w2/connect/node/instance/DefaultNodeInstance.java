package com.black.core.work.w2.connect.node.instance;

import com.black.core.json.Anatomy;
import com.black.core.json.Ignore;
import com.black.core.work.w2.connect.WorkflowInstance;
import com.black.core.work.w2.connect.node.WorkflowNode;
import lombok.Setter;

@Setter
public class DefaultNodeInstance implements NodeInstance {

    private boolean result, invoke, hasBlocking;

    @Anatomy(upAnalysis = true)
    private WorkflowNode relyNode;

    @Ignore
    private WorkflowInstance instance;
    private String invokeTime, finishTime;

    @Override
    public boolean getResult() {
        return result;
    }

    @Override
    public boolean isInvoke() {
        return invoke;
    }

    @Override
    public boolean hasBlocking() {
        return hasBlocking;
    }

    @Override
    public WorkflowNode getRelyNode() {
        return relyNode;
    }

    @Override
    public WorkflowInstance getInstance() {
        return instance;
    }

    @Override
    public String getInvokeTime() {
        return invokeTime;
    }

    @Override
    public String getFinishTime() {
        return finishTime;
    }
}
