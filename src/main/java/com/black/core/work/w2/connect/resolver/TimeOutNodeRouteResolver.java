package com.black.core.work.w2.connect.resolver;


import com.black.core.work.w2.connect.ConditionResolver;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.annotation.WriedWorkflowRouteResolver;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.node.TimeOutHandlerWorkflowNode;

@WriedWorkflowRouteResolver
public class TimeOutNodeRouteResolver extends AbstractWorkflowRouteResolver {

    public TimeOutNodeRouteResolver(ConditionResolver conditionResolver) {
        super(conditionResolver);
    }

    @Override
    public boolean support(WorkflowInstanceListener instanceListener, NodeInstance workflowNode) {
        return workflowNode.getRelyNode() instanceof TimeOutHandlerWorkflowNode;
    }

}
