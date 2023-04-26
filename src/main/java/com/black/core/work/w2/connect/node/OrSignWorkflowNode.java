package com.black.core.work.w2.connect.node;

import java.util.Map;

//@WorkflowAdaptationBuilder(OrSignNodeBuilder.class)
public class OrSignWorkflowNode extends AbstractWorkflowNode{

    public OrSignWorkflowNode(String name, WorkflowNodeDefinitional definitional) {
        super(name, definitional);
    }

    public OrSignWorkflowNode(String name, WorkflowNodeDefinitional definitional, Map<String, Object> attributeFormData) {
        super(name, definitional, attributeFormData);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
