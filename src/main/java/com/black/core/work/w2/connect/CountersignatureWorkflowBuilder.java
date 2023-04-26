package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.CountersignatureWorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNode;

public class CountersignatureWorkflowBuilder extends TimeOutWorkflowBuilder{


    public CountersignatureWorkflowBuilder(String status, String alias, WorkflowConfiguration configuration) {
        super(status, alias, configuration);
    }

    @Override
    public WorkflowNode relevance(WorkflowNode nextNode, Condition condition) {
        /**
         * 查看上一个节点是什么节点
         * 如果是会签节点, 则获取他所有的子节点, 然后将这些子节点
         * 与当前节点进行连线
         */
        PreNodeWrapper preNodeWrapper = preNodeLocal.get();
        if (preNodeWrapper != null){
            WorkflowNode preNode = preNodeWrapper.getPreNode();
            Condition wrapperCondition = preNodeWrapper.getCondition();
            if (preNode instanceof CountersignatureWorkflowNode){
                //如果进入当前处理环节， 则不会向上走, 需要将该节点注册进去
                workflowDefinitional.addNode(nextNode);

                CountersignatureWorkflowNode countersignatureWorkflowNode = (CountersignatureWorkflowNode) preNode;
//                List<WorkflowNode> approveNode = countersignatureWorkflowNode.getApproveNode();
//                for (WorkflowNode node : approveNode) {
//                    ConnectRouteWraper routeWraper = ConnectStaticFactory.create(node.name(), nextNode.name(), alias, wrapperCondition);
//                    workflowDefinitional.addConnect(routeWraper);
//                }
            }else {
                super.relevance(nextNode, condition);
            }
        }else {
            super.relevance(nextNode, condition);
        }

        /**
         * 如果当前节点是会签节点
         * 则获取当前节点下所有节点然后与他们进行连线
         */
        if (nextNode instanceof CountersignatureWorkflowNode){
            doHandler(nextNode, condition);
        }
        return nextNode;
    }
}
