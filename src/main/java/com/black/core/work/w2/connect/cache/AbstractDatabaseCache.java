package com.black.core.work.w2.connect.cache;

import com.black.core.work.w2.connect.entry.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDatabaseCache implements WorkflowDatabaseCache{

    /**
     * 工作流模板缓存
     * key = 工作流唯一别名
     */
    protected final Map<String, WorkflowEntry> workflowEntryCache = new ConcurrentHashMap<>();

    /***
     * 依旧存在活性的实例缓存
     * key = instance id
     */
    protected final Map<String, WorkflowInstanceEntry> activityInstanceCache = new ConcurrentHashMap<>();

    /**
     * 节点缓存, 用作查询
     * key = node module name
     */
    protected final Map<String, WorkflowNodeModuleEntry> nodeModuleEntryCache = new ConcurrentHashMap<>();

    /**
     * 节点缓存, 用作查询
     * key = node instance name
     */
    protected final Map<String, WorkflowNodeInstanceEntry> nodeInstanceEntryCache = new ConcurrentHashMap<>();

    /***
     * 路由缓存
     * key = route name
     * 展示每一个工作流有多少的分支
     */
    protected final Map<String, WorkflowRouteEntry> nodeRouteCache = new ConcurrentHashMap<>();

    @Override
    public void refrush() {
        refushWorkflow();

        refrushNodeModule();

        refrushNodeInstance();

        refrushRoute();

        refrushWorkflowInstance();
    }

    @Override
    public void refushWorkflow() {
        workflowEntryCache.clear();
        for (WorkflowEntry workflow : queryWorkflows()) {
            workflowEntryCache.put(workflow.getName(), workflow);
        }
    }

    public abstract List<WorkflowEntry> queryWorkflows();

    @Override
    public void refrushNodeModule() {
        nodeModuleEntryCache.clear();
        for (WorkflowNodeModuleEntry moduleEntry : queryNodeDefinitionals()) {
            nodeModuleEntryCache.put(moduleEntry.getName(), moduleEntry);
        }
    }

    public abstract List<WorkflowNodeModuleEntry> queryNodeDefinitionals();

    @Override
    public void refrushNodeInstance() {
        nodeInstanceEntryCache.clear();
        for (WorkflowNodeInstanceEntry queryNodeInstance : queryNodeInstances()) {
            nodeInstanceEntryCache.put(queryNodeInstance.getName(), queryNodeInstance);
        }
    }

    public abstract List<WorkflowNodeInstanceEntry> queryNodeInstances();

    @Override
    public void refrushWorkflowInstance() {
        activityInstanceCache.clear();
        for (WorkflowInstanceEntry queryInstance : queryInstances()) {
            activityInstanceCache.put(queryInstance.getId(), queryInstance);
        }
    }

    public abstract List<WorkflowInstanceEntry> queryInstances();

    @Override
    public void refrushRoute() {
        nodeRouteCache.clear();
        for (WorkflowRouteEntry route : queryRoutes()) {
            nodeRouteCache.put(route.getName(), route);
        }
    }

    public abstract List<WorkflowRouteEntry> queryRoutes();

    @Override
    public Map<String, WorkflowEntry> getWorkflowEntryCache() {
        return workflowEntryCache;
    }

    @Override
    public Map<String, WorkflowInstanceEntry> getActivityInstanceCache() {
        return activityInstanceCache;
    }

    @Override
    public Map<String, WorkflowNodeModuleEntry> getNodeModuleEntryCache() {
        return nodeModuleEntryCache;
    }

    @Override
    public Map<String, WorkflowNodeInstanceEntry> getNodeInstanceEntryCache() {
        return nodeInstanceEntryCache;
    }

    @Override
    public Map<String, WorkflowRouteEntry> getNodeRouteCache() {
        return nodeRouteCache;
    }

    @Override
    public void clear() {
        workflowEntryCache.clear();
        activityInstanceCache.clear();
        nodeModuleEntryCache.clear();
        nodeInstanceEntryCache.clear();
        nodeRouteCache.clear();
    }
}
