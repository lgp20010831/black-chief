package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.node.TimeOutHandlerWorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

@NodeBuilderAuthentication
public class TimeOutNodeBudiler extends AbstractNodeBuilder{

    @Override
    public boolean support(WorkflowNodeDefinitional definitional, NodeBuilderLeader leader) {
        return TimeOutHandlerWorkflowNode.class.isAssignableFrom(definitional.nodeType());
    }
}
