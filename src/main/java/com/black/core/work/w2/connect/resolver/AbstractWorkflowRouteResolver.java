package com.black.core.work.w2.connect.resolver;

import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.ill.ResolverConditionException;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.node.WorkflowNode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractWorkflowRouteResolver implements WorkflowRouteResolver {

    private final ConditionResolver conditionResolver;

    protected AbstractWorkflowRouteResolver(ConditionResolver conditionResolver) {
        this.conditionResolver = conditionResolver;
    }

    @Override
    public List<NodeInstance> flowTakeNodes(WorkflowInstanceListener instanceListener,
                                            NodeInstance nodeInstance) throws ResolverConditionException {
        lock.lock();
        try {
            WorkflowNode workflowNode = nodeInstance.getRelyNode();
            WorkflowProcessing workflowProcessing = instanceListener.getWorkflowProcessing();
            Map<String, NodeInstance> nodeInstances = instanceListener.getNodeInstances();
            WorkflowDefinitional definitional = workflowProcessing.getDefinitional();

            //获取路由地址
            Set<ConnectRouteWraper> routeWrapers = definitional.getRouteWrapers();

            //获取到该节点接下来的所有路由地址
            Set<ConnectRouteWraper> selfRoute = routeWrapers
                    .stream()
                    .filter(crw -> crw.getStartAlias().equals(workflowNode.name()))
                    .collect(Collectors.toSet());
            if (selfRoute.isEmpty()){

                if (nodeInstance.getRelyNode().isTail()){
                    instanceListener.setFinallyResult(true);
                }
                return new ArrayList<>();
            }
            List<NodeInstance> nextNodes = new ArrayList<>();
            for (ConnectRouteWraper routeWraper : selfRoute) {
                boolean routeResult;

                //获取条件表达式
                String conditionExpression = routeWraper.getConditionExpression();
                try {

                    if (StringUtils.hasText(conditionExpression)){
                        routeResult = conditionResolver.parseConditionExpression(conditionExpression, instanceListener, nodeInstance);
                    }else {
                        routeResult = conditionResolver.parseCondition(routeWraper.getCondition(), instanceListener, nodeInstance);
                    }
                }catch (Throwable e){
                    throw new ResolverConditionException(e);
                }

                //如果结果为 true
                //表示接下来 endAlias 是要路由的地点
                if (routeResult){

                    //将这次路由记录在监听者中
                    instanceListener.getInstance().addRoutePath(routeWraper);

                    //找到终点的节点
                    String endAlias = routeWraper.getEndAlias();
                    NodeInstance instance = nodeInstances.get(endAlias);
                    if (instance != null){
                        DefaultNodeInstance defaultNodeInstance = (DefaultNodeInstance) instance;
                        defaultNodeInstance.setInvoke(false);
                        nextNodes.add(instance);
                    }
                }
            }
            return nextNodes;
        }finally {
            lock.unlock();
        }
    }
}
