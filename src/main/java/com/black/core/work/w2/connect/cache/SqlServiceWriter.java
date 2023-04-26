package com.black.core.work.w2.connect.cache;


import com.alibaba.fastjson.JSONObject;
import com.black.core.spring.instance.PostConstr;
import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.check.DefaultWorkflowBalance;
import com.black.core.work.w2.connect.check.WorkflowBalance;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.entry.*;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;
import com.black.core.work.utils.WorkUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Log4j2
public class SqlServiceWriter extends AbstractDatabaseCache implements WriteDatabaseHandler{

    private final SqlWritorProxy proxy;

    WorkflowConfiguration configuration;

    private WorkflowBalance workflowBalance;

    public SqlServiceWriter(BeanFactory beanFactory, WorkflowConfiguration configuration){
        this.configuration = configuration;
        proxy = new SqlWritorProxy(this);
        checkTable();
    }


    public SqlWritorProxy getProxy() {
        return proxy;
    }

    @Override
    public WorkflowBalance getBalance() {
        return workflowBalance;
    }

    @Override
    public WorkflowMapper getMapper() {
        return WorkflowSessionManager.getMapper();
    }

    @Override
    public WorkflowInstance getInstance(String workflowId, String instanceId) {
        return proxy.getInstance(workflowId, instanceId);
    }

    @Override
    public WorkflowInstanceListener getListener(String instanceId, WorkflowProcessing workflowProcessing) {
        String id = workflowProcessing.id();
        WorkflowInstanceListener listener = null;
        WorkflowInstanceEntry instanceEntry = activityInstanceCache.get(instanceId);
        if (instanceEntry != null){
            listener = WorkflowEntryConvertFactory.convertInstanceListener(instanceEntry);
        }

        if (listener == null){
            listener = proxy.getListener(instanceId, workflowProcessing);
        }

        if (listener != null){
            listener.setWorkflowProcessing(workflowProcessing);
        }
        return listener;
    }

    @Override
    public List<WorkflowNodeInstanceDetailsEntry> readDetailsEntry(String instanceId) {
        return proxy.readDetailsEntry(instanceId);
    }

    @Override
    public String writeInstance(WorkflowInstanceListener instanceListener) {
        return proxy.writeInstance(instanceListener);
    }

    @Override
    public String writeNodeModule(WorkflowNodeDefinitional definitional) {
        return proxy.writeNodeModule(definitional);
    }


    @Override
    public void updateInstance(WorkflowInstanceListener listener) {
        proxy.updateInstance(listener);
    }

    @Override
    public void updateNodeModule(WorkflowNodeDefinitional definitional) {
        proxy.updateNodeModule(definitional);
    }

    //创建表
    //然后读取表里的内容
    protected void checkTable(){
       proxy.checkTable();
    }

    @PostConstr
    protected void fillCache(){
         refrush();
        if (workflowBalance == null){
            workflowBalance = new DefaultWorkflowBalance(this, this);
        }
    }


    @Override
    public WorkflowConfiguration getConfiguration() {
        return configuration;
    }


    //当一个任务开始时执行
    //将实例信息  和  节点实例信息 缓存到数据库
    //在将实例信息缓存到 map 中
    @Override
    public void startWork(WorkflowInstanceListener listener) {
        proxy.startWork(listener);
    }

    @Override
    public void finishTask(WorkflowInstanceListener listener, String result) {
       proxy.finishTask(listener, result);
    }

    @Override
    public void pauseTask(WorkflowInstanceListener listener) {
        proxy.pauseTask(listener);
    }

    @Override
    public void init(WorkflowProcessing engine) {
        Collection<WorkflowInstanceEntry> instances = activityInstanceCache.values();
        if (instances != null && !instances.isEmpty()){
            for (WorkflowInstanceEntry instanceEntry : instances) {
                if (engine.id().equals(instanceEntry.getWorkflowId())){
                    log.info("发现存活的工作流实例: 所属工作流:{}, 实例id:{}", instanceEntry.getWorkflowId(), instanceEntry.getId());
                    WorkflowInstanceListener instanceListener =  WorkflowEntryConvertFactory.convertInstanceListener(instanceEntry);
                    WorkflowInstance instance = instanceListener.getInstance();
                    JSONObject scheduledTime = instance.getScheduledTime();
                    scheduledTime.forEach((nodeId, time) ->{
                        long invokeTime = (Long.parseLong(time.toString())) - (System.currentTimeMillis() - WorkUtils.parserTimeStr(instance.updateTime()));
                        engine.submitTimerTask(instanceListener, nodeId, TimeUnit.MILLISECONDS, invokeTime);
                        log.info("提交实例任务, 实例id:{}", instanceListener.getInstance().id());
                    });
                }
            }
        }
    }

    @Override
    public void parkWorkflow(String name) {
        proxy.parkWorkflow(name);
    }

    @Override
    public void activitiWorkflow(String name) {
        proxy.activitiWorkflow(name);
    }

    @Override
    public String writeEngine(WorkflowDefinitional workflowDefinitional, boolean update) {
        return proxy.writeEngine(workflowDefinitional, update);
    }

    @Override
    public String writeRoute(ConnectRouteWraper routeWraper, WorkflowDefinitional definitional) {
        return proxy.writeRoute(routeWraper, definitional);
    }


    @Override
    public String writeNode(WorkflowNode workflowNode, WorkflowDefinitional definitional) {
        return proxy.writeNode(workflowNode, definitional);
    }

    @Override
    public void removeNode(String alias) {
        proxy.removeNode(alias);
    }

    @Override
    public List<WorkflowEntry> queryWorkflows() {
        return proxy.queryWorkflows();
    }

    @Override
    public List<WorkflowNodeModuleEntry> queryNodeDefinitionals() {
        return proxy.queryNodeDefinitionals();
    }

    @Override
    public List<WorkflowNodeInstanceEntry> queryNodeInstances() {
        return proxy.queryNodeInstances();
    }

    @Override
    public List<WorkflowInstanceEntry> queryInstances() {
        return proxy.queryInstances();
    }

    @Override
    public List<WorkflowRouteEntry> queryRoutes() {
        return proxy.queryRoutes();
    }
}
