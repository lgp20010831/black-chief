package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.node.instance.NodeInstance;

public interface ConditionResolver {

    boolean parseConditionExpression(String expression, WorkflowInstanceListener listener, NodeInstance node) throws Throwable;

    boolean parseCondition(Condition condition, WorkflowInstanceListener listener, NodeInstance node) throws Throwable;
}
