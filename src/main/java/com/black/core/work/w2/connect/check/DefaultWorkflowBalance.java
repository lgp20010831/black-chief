package com.black.core.work.w2.connect.check;

import com.black.core.work.w2.connect.cache.AbstractDatabaseCache;
import com.black.core.work.w2.connect.cache.WorkflowDatabaseCache;
import com.black.core.work.w2.connect.cache.WriteDatabaseHandler;
import com.black.core.work.w2.connect.entry.*;

import java.util.List;


public class DefaultWorkflowBalance extends AbstractDatabaseCache implements WorkflowBalance{

    private final WriteDatabaseHandler writeDatabaseHandler;

    private final AbstractDatabaseCache chain;

    public DefaultWorkflowBalance(WriteDatabaseHandler writeDatabaseHandler, AbstractDatabaseCache chain) {
        this.writeDatabaseHandler = writeDatabaseHandler;
        this.chain = chain;
        init();
    }

    protected void init(){
        WorkflowDatabaseCache workflowDatabaseCache = (WorkflowDatabaseCache) writeDatabaseHandler;
        workflowEntryCache.putAll(workflowDatabaseCache.getWorkflowEntryCache());
        activityInstanceCache.putAll(workflowDatabaseCache.getActivityInstanceCache());
        nodeModuleEntryCache.putAll(workflowDatabaseCache.getNodeModuleEntryCache());
        nodeInstanceEntryCache.putAll(workflowDatabaseCache.getNodeInstanceEntryCache());
        nodeRouteCache.putAll(workflowDatabaseCache.getNodeRouteCache());
    }


    @Override
    public void refrush() {
        clear();
        init();
    }

    @Override
    public List<WorkflowEntry> queryWorkflows() {
        return chain.queryWorkflows();
    }

    @Override
    public List<WorkflowNodeModuleEntry> queryNodeDefinitionals() {
        return chain.queryNodeDefinitionals();
    }

    @Override
    public List<WorkflowNodeInstanceEntry> queryNodeInstances() {
        return chain.queryNodeInstances();
    }

    @Override
    public List<WorkflowInstanceEntry> queryInstances() {
        return chain.queryInstances();
    }

    @Override
    public List<WorkflowRouteEntry> queryRoutes() {
        return chain.queryRoutes();
    }


}
