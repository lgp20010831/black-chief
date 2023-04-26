package com.black.core.work.w2.connect.config;

import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.work.w2.connect.WorkflowProcessing;
import com.black.core.work.w2.connect.builder.NodeBuilder;
import com.black.core.work.w2.connect.cache.SqlWritorType;
import com.black.core.work.w2.connect.cache.WorkflowDatabaseCache;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.WorkflowRefinedDispatcher;
import com.black.core.work.w2.connect.WorkflowRouteResolver;
import com.black.core.work.w2.connect.cache.WriteDatabaseHandler;
import com.black.core.work.w2.connect.runnable.WorkflowNodeRunnableHandler;
import com.black.core.work.w2.connect.time.WorkflowScheduledTaskDispatcher;
import com.black.core.work.w2.connect.tracker.WorkflowEventTracker;
import com.black.core.work.w2.service.WorkflowService;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter  @Setter
public class WorkflowConfiguration {

    //最终扩展到哪种类型的节点
    //然后构造出相应的 builder
    private Class<? extends WorkflowNode> extendNodeType;

    /**
     * 定时任务核心线程池数
     */
    private int corePoolSize;

    /***
     * 打印 sql 文件的执行 log
     */
    private boolean print;

    //异步执行
    private boolean asynTracker;

    //监听者线程池数
    private int trackerPoolSize = 1;

    private SqlWritorType writorType;

    private Class<? extends DataSourceBuilder> dataSourceIfMapSql;

    /**
     * 处理工作流各个节点之间路由的处理器
     */
    private Collection<WorkflowRouteResolver> routeResolvers;

    /**
     * 执行各个节点的处理逻辑
     */
    private Collection<WorkflowNodeRunnableHandler> runnableHandlers;

    /**
     * 操作数据库的工具
     */
    private WriteDatabaseHandler writeDatabaseHandler;

    /**
     * 工作流调度器
     */
    private WorkflowRefinedDispatcher dispatcher;

    /** 定时任务线程池 */
    private WorkflowScheduledTaskDispatcher scheduledTaskDispatcher;

    /**  service */
    private WorkflowService workflowServicce;

    /** 存贮所有监听者 */
    private final Map<String, Collection<WorkflowEventTracker>> trackers = new ConcurrentHashMap<>();

    private Collection<Class<? extends NodeBuilder>> builderTypes;

    public WorkflowDatabaseCache getDatabaseCache(){
        return (WorkflowDatabaseCache) writeDatabaseHandler;
    }

    public Collection<WorkflowEventTracker> getTrackerList(WorkflowProcessing processing){
        Collection<WorkflowEventTracker> workflowEventTrackers = trackers.get(processing.alias());
        if (workflowEventTrackers == null){
            return new HashSet<>();
        }
        return workflowEventTrackers;
    }
}
