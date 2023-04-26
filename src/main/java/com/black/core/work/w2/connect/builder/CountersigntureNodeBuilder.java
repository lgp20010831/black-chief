package com.black.core.work.w2.connect.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.core.builder.Col;
import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.ConnectRouteWraper;
import com.black.core.work.w2.connect.ConnectStaticFactory;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.ill.UnknowNodeDefinitionalException;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.node.*;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.black.core.work.w2.connect.ill.WorkflowAttributeKeys.*;

@NodeBuilderAuthentication
public class CountersigntureNodeBuilder extends AbstractNodeBuilder{


    @Override
    public boolean support(WorkflowNodeDefinitional definitional, NodeBuilderLeader leader) {
        return CountersignatureWorkflowNode.class.isAssignableFrom(definitional.nodeType());
    }

    @Override
    public WorkflowNode doBuild(WorkflowNodeDefinitional definitional, NodeBuilderLeader leader, Map<String, Object> formData, Condition condition, String nodeName) {
        //创建节点
        String defaultName = StringUtils.hasText(nodeName) ? nodeName : NodeFactory.createDefaultName(definitional.name(), leader.getAlias());
        WorkflowNode node = NodeFactory.create(definitional, Col.ar(defaultName, definitional, formData));

        //注册节点
        leader.getWorkflowDefinitional().addNode(node);
        setLevel(leader, node);

        //子节点
        List<WorkflowNode> nodes = new ArrayList<>();
        Set<String> approveNames = new HashSet<>();
        //判断是否指定了子节点
        boolean proxy = formData.containsKey(WorkflowAttributeKeys.SUB_NODES);
        if (proxy) {

            //更换新的层级
            //让子节点成为下一个层级
            Integer lel = leader.getLevelLocal().get() + 1;

            JSONObject json = new JSONObject(formData);
            //遍历子节点去与会签节点建立连线
            for (Object name : json.getJSONArray(WorkflowAttributeKeys.SUB_NODES)) {
                JSONObject subNodeBody = JSON.parseObject(JSON.toJSONString(name));
                String nn = subNodeBody.getString(MODULE_NAME);
                if (!StringUtils.hasText(nn)){
                    throw new RuntimeException("会签节点子节点模板名不能为空");
                }
                WorkflowNodeDefinitional subDefinitional = leader.getDefinitionalMap().get(nn);
                if (subDefinitional == null){
                    throw new UnknowNodeDefinitionalException("找不到子节点模板: " + nn);
                }

                //为子节点创建名字
                String mn0 = subNodeBody.getString(NODE_NAME);
                String mn = mn0 == null ? NodeFactory.createDefaultName(subDefinitional.name(), leader.getAlias()) : mn0;
                approveNames.add(mn);

                //创建子节点
                WorkflowNode subNode = NodeFactory.create(subDefinitional, Col.ar(mn, subDefinitional, subNodeBody));

                //给子节点设置新的层级
                subNode.setIndex(lel);

                //将子节点加入到 definitional中
                nodes.add(subNode);
                leader.getWorkflowDefinitional().addNode(subNode);
                ConnectRouteWraper routeWraper = ConnectStaticFactory.create(node.name(), subNode.name(), leader.getAlias(), (ler, ni) -> true);
                leader.getWorkflowDefinitional().addConnect(routeWraper);
            }
            leader.getLevelLocal().set(lel);
        }

        ThreadLocal<PreWrapper> local = leader.getWrapperLocal();
        PreWrapper wrapper = local.get();
        if (wrapper != null){

            //执行上一个节点的连线逻辑
            wrapper.getAddition().addition(node, condition);
        }

        //设置自己的连线逻辑
        local.set(new PreWrapper(node, createAddition(nodes, proxy, leader, approveNames, node)));
        return node;
    }

    protected AgentAddition createAddition(List<WorkflowNode> subNodes,
                                           boolean proxy,
                                           NodeBuilderLeader leader,
                                           Set<String> approveNames,
                                           WorkflowNode node){
        return (n, c) -> {
            if (proxy){
                //如果有子节点
                //将下一个添加的节点与所有子节点进行连线
                for (WorkflowNode workflowNode : subNodes) {
                    ConnectRouteWraper routeWraper = ConnectStaticFactory.create(workflowNode.name(),
                            n.name(), leader.getAlias(), (ler, ni) -> {
                                //设置路由条件, 当所有子节点的结果都为 true 时, 才能走向下一个节点
                                return createSignRule(ler, approveNames);
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

    protected boolean createSignRule(WorkflowInstanceListener ler, Set<String> approveNames){
        //设置路由条件, 当所有子节点的结果都为 true 时, 才能走向下一个节点
        boolean successful = true;
        Map<String, NodeInstance> instanceMap = ler.getNodeInstances();
        for (String name : approveNames) {
            NodeInstance nodeInstance = instanceMap.get(name);
            if (!nodeInstance.getResult()){
                successful = false;
                break;
            }
        }
        return successful;
    }
}
