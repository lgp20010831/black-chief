package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.Map;

public interface NodeBuilder {

    boolean support(WorkflowNodeDefinitional definitional, NodeBuilderLeader leader);


    WorkflowNode doBuild(WorkflowNodeDefinitional definitional,
                         NodeBuilderLeader leader,
                         Map<String, Object> formData,
                         Condition condition,
                         String nodeName);

}
