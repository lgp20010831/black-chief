package com.black.core.work.w2.connect.entry;


import com.black.core.work.w2.connect.node.WorkflowNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNodeModuleEntry {

    String id, name, attributeKeys;

    public WorkflowNodeModuleEntry(WorkflowNode workflowNode){
        id = UUID.randomUUID().toString();
        name = workflowNode.name();
    }

}
