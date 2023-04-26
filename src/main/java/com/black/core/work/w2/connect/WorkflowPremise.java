package com.black.core.work.w2.connect;

import com.black.core.chain.ChainPremise;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.work.w2.connect.annotation.EnableWorkflowRefinedModule;
import org.springframework.core.annotation.AnnotationUtils;

public class WorkflowPremise implements ChainPremise {

    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && AnnotationUtils.getAnnotation(mainClass, EnableWorkflowRefinedModule.class) != null;
    }
}
