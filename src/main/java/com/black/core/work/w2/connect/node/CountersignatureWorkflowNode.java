package com.black.core.work.w2.connect.node;

import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.annotation.WorkflowAdaptationBuilder;

import java.util.*;

//会签节点
@WorkflowAdaptationBuilder(CountersignatureWorkflowBuilder.class)
public class CountersignatureWorkflowNode extends AbstractWorkflowNode {

    public CountersignatureWorkflowNode(String name, WorkflowNodeDefinitional definitional){
        super(name, definitional);
    }

    public CountersignatureWorkflowNode(String name, WorkflowNodeDefinitional definitional, Map<String, Object> attributeFormData){
        super(name, definitional, attributeFormData);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

}
