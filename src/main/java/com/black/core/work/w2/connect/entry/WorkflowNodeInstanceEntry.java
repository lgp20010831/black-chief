package com.black.core.work.w2.connect.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNodeInstanceEntry {
    String id, name, moduleId, moduleName, workflowId, workflowName, attributes;
    Integer level;
    Boolean head, tail;
}
