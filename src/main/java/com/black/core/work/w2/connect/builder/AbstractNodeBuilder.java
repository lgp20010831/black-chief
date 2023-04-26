package com.black.core.work.w2.connect.builder;

import com.black.core.builder.Col;
import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.ConnectRouteWraper;
import com.black.core.work.w2.connect.ConnectStaticFactory;
import com.black.core.work.w2.connect.node.NodeFactory;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;
import org.springframework.util.StringUtils;

import java.util.Map;

public abstract class AbstractNodeBuilder implements NodeBuilder{

    @Override
    public WorkflowNode doBuild(WorkflowNodeDefinitional definitional,
                                NodeBuilderLeader leader,
                                Map<String, Object> formData,
                                Condition condition,
                                String nodeName) {
        //创建节点
        String defaultName = StringUtils.hasText(nodeName) ? nodeName : NodeFactory.createDefaultName(definitional.name(), leader.getAlias());
        WorkflowNode node = NodeFactory.create(definitional, Col.ar(defaultName, definitional, formData));

        //注册节点
        leader.getWorkflowDefinitional().addNode(node);

        //设置层级
        setLevel(leader, node);

        ThreadLocal<PreWrapper> local = leader.getWrapperLocal();
        PreWrapper wrapper = local.get();
        if (wrapper != null){

            //执行上一个节点的逻辑
            wrapper.getAddition().addition(node, condition);
        }

        //设置当前节点的逻辑
        local.set(createWrapper(node, leader));

        return node;
    }

    protected PreWrapper createWrapper(WorkflowNode node, NodeBuilderLeader leader){
        return new PreWrapper(node, (n, c) -> {
            ConnectRouteWraper routeWraper = ConnectStaticFactory.create(node.name(), n.name(), leader.getAlias(), c);
            leader.getWorkflowDefinitional().addConnect(routeWraper);
        });
    }

    protected void setLevel(NodeBuilderLeader leader, WorkflowNode node){
        ThreadLocal<Integer> levelLocal = leader.getLevelLocal();
        int i = levelLocal.get() + 1;
        node.setIndex(i);
        levelLocal.set(i);
    }
}
