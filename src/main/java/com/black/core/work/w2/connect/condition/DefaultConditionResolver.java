package com.black.core.work.w2.connect.condition;

import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.ConditionResolver;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

public class DefaultConditionResolver implements ConditionResolver {

    @Override
    public boolean parseConditionExpression(String expression, WorkflowInstanceListener listener, NodeInstance node) throws Throwable {
        throw new RuntimeException("该解析器无法解析条件表达式");
    }

    @Override
    public boolean parseCondition(Condition condition, WorkflowInstanceListener listener, NodeInstance node) throws Throwable {
        return condition.processor(listener, node);
    }
}
