package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.node.DefaultHandlerWorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;


@NodeBuilderAuthentication
public class DefaultNodeBuilder extends AbstractNodeBuilder{


    @Override
    public boolean support(WorkflowNodeDefinitional definitional, NodeBuilderLeader leader) {
        return DefaultHandlerWorkflowNode.class.isAssignableFrom(definitional.nodeType());
    }


}
