package com.black.core.work.w2.connect.test;


import com.black.core.builder.Col;
import com.black.core.work.w2.connect.WorkflowAdaptation;
import com.black.core.work.w2.connect.WorkflowDefinitional;
import com.black.core.work.w2.connect.builder.NodeBuilderLeader;


//建一个请假的流程
//@WorkflowDefinition("qj")
public class QJ implements WorkflowAdaptation {


    @Override
    public WorkflowDefinitional getEngine(NodeBuilderLeader leader) {
        leader.add("审批人通用节点", Col.js("approveMan", "审批人1"));
        leader.add("审批人通用节点", Col.js("approveMan", "审批人2"));
        return leader.end();
    }
}
