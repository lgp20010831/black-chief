package com.black.core.work.w2.connect.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowHistoryInstanceEntry {

    String instanceId, workflowId, routePath, createTime, updateTime, result, formData, properties;
}
