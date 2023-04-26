package com.black.core.work.w1;


import com.black.core.chain.*;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.work.w1.cache.CacheTask;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@AddHolder @ChainClient(WorkFlowScheduler.class)
@LazyLoading(EnableWorkflowModule.class)
public class WorkFlowScheduler implements OpenComponent, EnabledControlRisePotential,
        CollectedCilent, ApplicationDriver, ChainPremise {

    private InstanceFactory instanceFactory;
    private CacheTask cacheTask;
    private final String resolver_alias = "WorkFlowScheduler -- resolver";
    private final String config_alias = "WorkFlowScheduler -- config";
    private List<TaskNodeResolver<Boolean>> resolvers;
    EnableWorkflowModule workflowModule;
    private final Map<String, TaskFlowQueue> taskQueueCache = new HashMap<>();
    private Collection<Object> earlySource;

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        WorkFlowSchedulerCache.workFlowScheduler = this;

        //初始化, 重新加载回所有的任务
        taskQueueCache.forEach((alias, queue) ->{
            queue.init();
        });
    }

    public TaskGlobalListener createInstance(String alias, Map<String, Object> source){
        TaskFlowQueue taskFlowQueue = taskQueueCache.get(alias);
        if (taskFlowQueue != null){
            return taskFlowQueue.run(source);
        }
        return null;
    }

    public TaskGlobalListener completeInstance(String alias, String taskId, Boolean result){
        TaskFlowQueue queue = taskQueueCache.get(alias);
        if (queue != null){
            return queue.complete(taskId, result);
        }
        return null;
    }


    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        EnableWorkflowModule workflowModule = (EnableWorkflowModule) annotation;
        if (instanceFactory == null){
            instanceFactory = application.instanceFactory();
        }
        this.workflowModule = workflowModule;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry begin = register.begin();
        begin.setAlias(resolver_alias);
        begin.needOrder(false);
        begin.condition(h ->
                TaskNodeResolver.class.isAssignableFrom(h) &&
                        AnnotationUtils.findAnnotation(h, WorkflowResolver.class) != null);


        ConditionEntry entry = register.begin();
        entry.setAlias(config_alias);
        entry.needOrder(false);
        entry.condition(h ->
            ContributionWorkFlowQueue.class.isAssignableFrom(h) && BeanUtil.isSolidClass(h)
                    && AnnotationUtils.getAnnotation(h, WorkflowTemplate.class) != null);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        try {

            if (cacheTask == null && workflowModule != null){
                cacheTask = instanceFactory.getInstance(workflowModule.value());
            }
            if (resultBody.getAlias().equals(resolver_alias)) {
                resolvers = resultBody.getCollectSource().stream()
                        .map(m -> (TaskNodeResolver<Boolean>)m).collect(Collectors.toList());
            }

            if (resultBody.getAlias().equals(config_alias)){
                earlySource = resultBody.getCollectSource();
            }
        }finally {
            if (resolvers != null && earlySource != null){
                IfConditionAndHandlerAdapter adapter = new IfConditionAndHandlerAdapter(cacheTask, resolvers, instanceFactory);
                for (Object sc : earlySource) {
                    ContributionWorkFlowQueue contributionWorkFlowQueue = (ContributionWorkFlowQueue) sc;
                    String alias = contributionWorkFlowQueue.alias();
                    if (taskQueueCache.containsKey(alias)){
                        throw new RuntimeException("此工作流模板已经存在了 " + alias);
                    }
                    TaskFlowQueue flowQueue = contributionWorkFlowQueue.giveTemplate(adapter);
                    flowQueue.setAlias(alias);
                    if (flowQueue != null){
                        taskQueueCache.put(alias, flowQueue);
                    }
                }
                if (log.isInfoEnabled()) {
                    log.info("work flow template: {}", taskQueueCache.keySet());
                }
                earlySource.clear();
            }
        }
    }


    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        //当服务停止时, 关闭所有模板队列
        taskQueueCache.forEach((alias, queue) ->{
            queue.close();
        });
    }

    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && AnnotationUtils.getAnnotation(mainClass, EnableWorkflowModule.class) != null;
    }
}
