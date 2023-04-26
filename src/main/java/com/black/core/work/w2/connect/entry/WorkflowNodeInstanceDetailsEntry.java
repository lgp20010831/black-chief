package com.black.core.work.w2.connect.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNodeInstanceDetailsEntry {

    private String instanceId, nodeInstanceId, invokeTime, finishTime, nodeName;
    private Boolean invoke, hasBlocking, result;
    private Integer level;
}
