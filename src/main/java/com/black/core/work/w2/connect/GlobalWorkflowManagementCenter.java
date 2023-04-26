package com.black.core.work.w2.connect;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.work.w1.DefaultWorkflowBuilder;
import com.black.core.work.w2.connect.annotation.WorkflowAdaptationBuilder;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.WorkflowNode;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//这里保存收集到的所有工作流之间的关联
public final class GlobalWorkflowManagementCenter {

    // key = workflow alias value = 连线
    private final Map<String, Set<ConnectRouteWraper>> workflowRouteCache = new HashMap<>();

    //key = workflow alias , value = nodes
    private final Map<String, List<WorkflowNode>> workflowNodeCache = new HashMap<>();

    private WorkflowConfiguration configuration;

    private static GlobalWorkflowManagementCenter instance;

    private static ThreadPoolExecutor trackerPool;

    public static GlobalWorkflowManagementCenter getInstance(){
        if (instance == null){
            instance = new GlobalWorkflowManagementCenter();
        }
        return instance;
    }

    public Map<String, Set<ConnectRouteWraper>> getWorkflowRouteCache() {
        return workflowRouteCache;
    }


    public Map<String, List<WorkflowNode>> getWorkflowNodeCache() {
        return workflowNodeCache;
    }

    public void callBack(WorkflowDefinitional definitional){
        String alias = definitional.alias;
        workflowRouteCache.put(alias, definitional.getRouteWrapers());
        workflowNodeCache.put(alias, definitional.nodeAliasSet);
    }

    public void setWorkflowConfiguration(WorkflowConfiguration workflowConfiguration) {
        this.configuration = workflowConfiguration;
    }

    public WorkflowBuilder getWorkflowBuilder(String status, String alias) {
        WorkflowBuilder workflowBuilder;
        if (configuration == null){
            workflowBuilder = new DefaultWorkflowBuilder(status, alias, configuration);
        }else {
            workflowBuilder = reflexCreateBuilder(configuration.getExtendNodeType(), status, alias, configuration);
        }
        return workflowBuilder;
    }

    public WorkflowBuilder reflexCreateBuilder(Class<? extends WorkflowNode> extendNodeType, String status, String alias, WorkflowConfiguration workflowConfiguration){
        WorkflowAdaptationBuilder adaptationBuilder = AnnotationUtils.getAnnotation(extendNodeType, WorkflowAdaptationBuilder.class);
        if (adaptationBuilder == null){
            throw new RuntimeException("节点需要绑定设配的 WorkflowBuilder");
        }
        Class<? extends WorkflowBuilder> clazz = adaptationBuilder.value();
        try {
            Constructor<? extends WorkflowBuilder> constructor = clazz.getConstructor(String.class, String.class, WorkflowConfiguration .class);
            return constructor.newInstance(status, alias, workflowConfiguration);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }


    public void asynInvokeTracker(Runnable runnable){
        if (trackerPool == null){
            trackerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(configuration.getTrackerPoolSize());
        }
        try {
            trackerPool.execute(runnable);
        }catch (RuntimeException ex){
            CentralizedExceptionHandling.handlerException(ex);
        }
    }

}
