package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.node.WorkflowNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PreWrapper {

    WorkflowNode preNode;
    AgentAddition addition;

}
