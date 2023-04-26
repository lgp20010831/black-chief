package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.ConnectRouteWraper;
import com.black.core.work.w2.connect.ConnectStaticFactory;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.node.OrSignWorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NodeBuilderAuthentication
public class OrSignNodeBuilder extends CountersigntureNodeBuilder{

    @Override
    public boolean support(WorkflowNodeDefinitional definitional, NodeBuilderLeader leader) {
        return OrSignWorkflowNode.class.isAssignableFrom(definitional.nodeType());
    }

    @Override
    protected AgentAddition createAddition(List<WorkflowNode> subNodes, boolean proxy, NodeBuilderLeader leader, Set<String> approveNames, WorkflowNode node) {
        return (n, c) -> {
            if (proxy){
                //如果有子节点
                //将下一个添加的节点与所有子节点进行连线
                for (WorkflowNode workflowNode : subNodes) {
                    ConnectRouteWraper routeWraper = ConnectStaticFactory.create(workflowNode.name(),
                            n.name(), leader.getAlias(), (ler, ni) -> {

                                //设置路由条件, 其他节点只要有一个为 true, 则返回 false
                                Map<String, NodeInstance> instanceMap = ler.getNodeInstances();
                                for (String name : approveNames) {
                                    NodeInstance nodeInstance = instanceMap.get(name);
                                    if (nodeInstance.getResult()){
                                        return false;
                                    }
                                }
                                //如果其他节点都为 false
                                //则判断当前节点是否为 true
                                return ni.getResult();
                            });

                    leader.getWorkflowDefinitional().addConnect(routeWraper);
                }
            }else {

                //如果没有子节点, 则创建默认的规则
                ConnectRouteWraper routeWraper = ConnectStaticFactory.create(node.name(), n.name(), leader.getAlias(), c);
                leader.getWorkflowDefinitional().addConnect(routeWraper);
            }
        };
    }
}
