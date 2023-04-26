package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.ill.ResolverConditionException;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public interface WorkflowRouteResolver {

    ReentrantLock lock = new ReentrantLock();

    //是否支持路由
    boolean support(WorkflowInstanceListener instanceListener, NodeInstance workflowNode);

    //进行路由
    List<NodeInstance> flowTakeNodes(WorkflowInstanceListener instanceListener, NodeInstance workflowNode) throws ResolverConditionException;

}
