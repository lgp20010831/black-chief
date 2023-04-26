package com.black.core.work.w2.connect.node;

import com.black.core.work.w1.DefaultWorkflowBuilder;
import com.black.core.work.w2.connect.annotation.WorkflowAdaptationBuilder;

import java.util.Map;

//默认的节点
@WorkflowAdaptationBuilder(DefaultWorkflowBuilder.class)
public class DefaultHandlerWorkflowNode extends AbstractWorkflowNode {


    public DefaultHandlerWorkflowNode(String name, WorkflowNodeDefinitional definitional) {
        super(name, definitional);
    }

    public DefaultHandlerWorkflowNode(String name, WorkflowNodeDefinitional definitional, Map<String, Object> attributeFormData) {
        super(name, definitional, attributeFormData);
    }
}
