package com.black.core.work.w2.connect.config;

import com.black.core.spring.instance.InstanceFactory;
import com.black.core.util.AnnotationUtils;
import com.black.core.work.w2.connect.annotation.EnableWorkflowRefinedModule;

public final class AnnotationConfigurationProcessor {

    private final EnableWorkflowRefinedModule workflowRefinedModule;
    private final InstanceFactory instanceFactory;

    public AnnotationConfigurationProcessor(EnableWorkflowRefinedModule workflowRefinedModule, InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
        this.workflowRefinedModule = workflowRefinedModule;
    }

    public WorkflowConfiguration handler(){
        WorkflowConfiguration workflowConfiguration =instanceFactory.getInstance(WorkflowConfiguration.class);
        AnnotationUtils.loadAttribute(workflowRefinedModule, workflowConfiguration);
        return workflowConfiguration;
    }
}
