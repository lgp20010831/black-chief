package com.black.core.work.w2.connect.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRouteEntry {

    String id, name, workflowId, startAlias, endAlias, startNodeId, endNodeId, conditional;
}
