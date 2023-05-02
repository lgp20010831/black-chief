package com.black.core.work.w2.connect;

import com.black.core.chain.ChainPremise;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.work.w2.connect.annotation.EnableWorkflowRefinedModule;

public class WorkflowPremise implements ChainPremise {

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnableWorkflowRefinedModule.class);
    }
}
