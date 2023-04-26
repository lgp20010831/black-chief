package com.black.core.work.w2.connect.node;

import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;

import java.util.List;

public interface WorkflowNodeDefinitional {

    String id();

    void setId(String id);

    String name();

    Class<? extends WorkflowNode> nodeType();

    List<String> attributeKeys();

    WorkflowNodeDefinitional addAttributeKey(String key);

    WorkflowRunnable getRunnable();

    WorkflowConfiguration getConfiguration();
}
