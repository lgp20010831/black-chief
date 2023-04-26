package com.black.core.work.w2.connect.node;

import com.black.core.work.w2.connect.TimeOutWorkflowBuilder;
import com.black.core.work.w2.connect.annotation.WorkflowAdaptationBuilder;

import java.util.Map;

//类似于审批节点, 是可以定时堵塞的

@WorkflowAdaptationBuilder(TimeOutWorkflowBuilder.class)
public class TimeOutHandlerWorkflowNode extends AbstractWorkflowNode {


    public TimeOutHandlerWorkflowNode(String name, WorkflowNodeDefinitional definitional){
        super(name, definitional);
    }


    public TimeOutHandlerWorkflowNode(String name, WorkflowNodeDefinitional definitional,
                                        Map<String, Object> attributeFormData) {
        super(name, definitional, attributeFormData);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

}
