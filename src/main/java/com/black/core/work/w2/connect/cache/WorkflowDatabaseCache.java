package com.black.core.work.w2.connect.cache;

import com.black.core.work.w2.connect.entry.*;

import java.util.Map;

public interface WorkflowDatabaseCache {

    void refrush();

    Map<String, WorkflowEntry> getWorkflowEntryCache();

    Map<String, WorkflowInstanceEntry> getActivityInstanceCache();

    Map<String, WorkflowNodeModuleEntry> getNodeModuleEntryCache();

    Map<String, WorkflowNodeInstanceEntry> getNodeInstanceEntryCache();

    Map<String, WorkflowRouteEntry> getNodeRouteCache();

    void refushWorkflow();

    void refrushNodeModule();

    void refrushNodeInstance();

    void refrushWorkflowInstance();

    void refrushRoute();

    void clear();


}
