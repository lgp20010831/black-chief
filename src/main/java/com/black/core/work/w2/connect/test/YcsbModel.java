package com.black.core.work.w2.connect.test;


import com.black.core.builder.Col;
import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.builder.NodeBuilderLeader;
import com.black.core.work.w2.connect.node.*;
import com.black.core.work.w2.connect.node.definition.CountersignatureNodeDefinitional;
import com.black.core.work.w2.connect.node.definition.OrsignNodeDefinitional;
import com.black.core.work.w2.connect.node.definition.TimeOutNodeDefinitional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


//@WorkflowDefinition("ycsb")  @WriedWorkflowNodes
public class YcsbModel implements WorkflowAdaptation, WorkflowNodeDefinitionalAdapation {

    @Override
    public WorkflowDefinitional getEngine(NodeBuilderLeader leader) {
        leader.add("审批人通用节点", Col.js("approveMan", "张总", "timeOutUnit", TimeUnit.SECONDS, "deadLine", 90));
        leader.add("审批人通用节点", Col.js("approveMan", "王总", "timeOutUnit", TimeUnit.SECONDS, "deadLine", 90));
        leader.add("会签通用节点", Col.js("counterMan", "张总",
                "subNodes", Col.ja(
                        Col.js("moduleName", "审批人通用节点", "approveMan", "小三"),
                        Col.js("moduleName", "审批人通用节点", "approveMan", "小四"),
                        Col.js("moduleName", "审批人通用节点", "approveMan", "小五")
                )));


        leader.add("审批人通用节点", Col.js("approveMan", "董事长"));
        return leader.end();
    }

    @Override
    public List<WorkflowNodeDefinitional> getNodes() {
        List<WorkflowNodeDefinitional> nodeList = new ArrayList<>();

        //添加一个通用的审批模板
        nodeList.add(new TimeOutNodeDefinitional("审批人通用节点", (lsr, wfn) -> {
            String approveMan = wfn.getString("approveMan");
            System.out.println(approveMan + "进行审批 ....");
        }, "approveMan", "list"));

        nodeList.add(new CountersignatureNodeDefinitional("会签通用节点", (lsr, wfn) -> {
            String counterMan = wfn.getString("counterMan");
            System.out.println("会签处理人: " + counterMan);
        }, "counterMan"));

        nodeList.add(new OrsignNodeDefinitional("或签通用节点", (lsr, wfn) ->{
            String counterMan = wfn.getString("counterMan");
            System.out.println("或签处理人: " + counterMan);
        }, "counterMan"));

        nodeList.add(new TimeOutNodeDefinitional("供应商基本信息节点", (lsr, wfn) ->{
            System.out.println("等待采购部审核");
        }));
        return nodeList;
    }
}
