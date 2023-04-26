package com.black.core.work.w2.connect.node.definition;

import com.black.core.work.w1.WorkFlowSchedulerCache;
import com.black.core.work.w2.connect.WorkflowRefinedDispatcher;
import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractNodeDefinitional implements WorkflowNodeDefinitional {

    protected String id, name;
    protected List<String> attributeKeys = new ArrayList<>();
    protected WorkflowRunnable runnable;
    protected Class<? extends WorkflowNode> nodeType;
    protected WorkflowConfiguration configuration;


    public AbstractNodeDefinitional(String name, Class<? extends WorkflowNode> nodeType, String... attributeKeys){
        this(name, nodeType, null, attributeKeys);
    }

    public AbstractNodeDefinitional(String name, Class<? extends WorkflowNode> nodeType, WorkflowRunnable runnable, String... attributeKeys){
        this.nodeType = nodeType;
        this.name = name;
        this.runnable = runnable;
        WorkflowRefinedDispatcher dispatcher = WorkFlowSchedulerCache.getWorkflowRefinedDispatcher();
        if (dispatcher != null){
            configuration = dispatcher.getWorkflowConfiguration();
        }
        for (String key : attributeKeys) {
            addAttributeKey(key);
        }
    }

    @Override
    public Class<? extends WorkflowNode> nodeType() {
        return nodeType;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> attributeKeys() {
        return attributeKeys;
    }

    @Override
    public WorkflowRunnable getRunnable() {
        return runnable;
    }

    @Override
    public WorkflowNodeDefinitional addAttributeKey(String key) {
        attributeKeys.add(key);
        return this;
    }

    @Override
    public WorkflowConfiguration getConfiguration() {
        return configuration;
    }
}
