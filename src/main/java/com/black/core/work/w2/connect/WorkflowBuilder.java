package com.black.core.work.w2.connect;

import com.black.core.work.w2.connect.node.WorkflowNode;

import java.util.Map;

//已经声明了一种类型的
public interface WorkflowBuilder {

    //结束返回一个身份定义
    WorkflowDefinitional build();

    WorkflowDefinitional getDefinitional();

    //默认实现
    default WorkflowNode relevance(WorkflowNode nextNode){
        return relevance(nextNode, (ler, wfn) -> wfn.getResult());
    }

    WorkflowDrawer getDrawer();

    //添加一个节点, 会自动与上一个节点划线
    //这里的 condition 会加载到与下一个节点的条件连线上
    WorkflowNode relevance(WorkflowNode nextNode, Condition condition);

    WorkflowNode put(String moduleName, String nodeName, Map<String, Object> attributeFormData, Condition condition);

    WorkflowNode put(String moduleName, String nodeName, Condition condition);

    WorkflowNode put(String moduleName, Map<String, Object> attributeFormData, Condition condition);

    WorkflowNode put(String moduleName, Condition condition);

    WorkflowNode put(String moduleName, Map<String, Object> attributeFormData);

    WorkflowNode put(String moduleName);
}
