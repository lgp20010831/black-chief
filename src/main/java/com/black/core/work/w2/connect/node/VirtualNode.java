package com.black.core.work.w2.connect.node;

import java.util.Map;

/**
 * 此节点要实现动态节点
 *
 */
public class VirtualNode extends AbstractWorkflowNode{

    public VirtualNode(String name, WorkflowNodeDefinitional definitional) {
        super(name, definitional);
    }

    public VirtualNode(String name, WorkflowNodeDefinitional definitional, Map<String, Object> attributeFormData) {
        super(name, definitional, attributeFormData);
    }
}
