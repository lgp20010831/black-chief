package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.ill.BlockingException;
import com.black.core.work.w2.connect.ill.ResolverConditionException;
import com.black.core.work.w2.connect.ill.UnknowNodeInstanceException;
import com.black.core.work.w2.connect.ill.WorkflowNodeRunnableException;
import com.black.core.work.w2.connect.node.NodeFactory;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.runnable.WorkflowNodeRunnableHandler;
import com.black.core.work.w2.connect.time.ScheduledNodeFutureWrapper;
import com.black.core.work.utils.WorkUtils;
import com.black.core.work.w2.connect.tracker.WorkflowEventTracker;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("all") @Log4j2
public class DefaultWorkflowProcessing implements WorkflowProcessing{

    private String id, alias;

    //节点队列
    private List<WorkflowNode> nodeQueue;

    //自己的身份定义
    private WorkflowDefinitional workflowDefinitional;

    @Setter
    private WorkflowRefinedDispatcher dispatcher;

    @Setter
    //配置类
    private WorkflowConfiguration configuration;

    private final ReentrantLock lock = new ReentrantLock();

    private final Map<String, WorkflowInstanceListener> listenerCache = new ConcurrentHashMap<>();

    public DefaultWorkflowProcessing(WorkflowDefinitional definitional){
        workflowDefinitional = definitional;
        alias = definitional.getAlias();
        nodeQueue = definitional.getNodeAliasSet();

    }

    /**
     *
     * 数据表:
     *      关系连线数据表
     *      记录了每个节点可能要走的所有路
     *      要有一个处理器在任务开始时， 拿到该队列所拥有的路由表
     *      然后拿到每条路径路由的条件,
     *      数据表里会存条件, 但这个条件必须很简单, 需要复杂的条件需要在程序中写
     * 每个节点都有自己的 id, 缓存到数据库中
     * 队列不再严格控制下一个要执行的节点是哪个
     *
     * 关系
     *          node
     *            ↓  → {@link WorkflowRouteResolver}
     *          node
     *
     */

    @Override
    public void init(WorkflowDefinitional definitional, WorkflowRefinedDispatcher dispatcher) {
        configuration.getWriteDatabaseHandler().init(this);
    }

    @Override
    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        for (WorkflowNode workflowNode : nodeQueue) {
            workflowNode.setWorkflowId(id);
        }
    }

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public List<WorkflowNode> getNodeQueue() {
        return nodeQueue;
    }

    @Override
    public WorkflowDefinitional getDefinitional() {
        return workflowDefinitional;
    }

    @Override
    public WorkflowConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void submitTimerTask(WorkflowInstanceListener listener, String nodeId, TimeUnit unit, long life) {
        configuration.getScheduledTaskDispatcher().doDispatcher(listener, nodeId, this, unit, life);
    }

    @Override
    public WorkflowInstanceListener complete(String taskId, String nodeId, boolean result) {

        lock.lock();
        try {

            if (log.isInfoEnabled()) {
                log.info("完成一个节点, 实例id: {}, 节点id: {}, 结果: {}", taskId, nodeId, result);
            }
            WorkflowInstanceListener listener = findListener(taskId);
            listener.complete(nodeId, result);

            //取消掉定时任务
            configuration.getScheduledTaskDispatcher().cancel(listener, nodeId);

            //有问题的点
            //要根据当前完成的节点 id, 设置当前实例要进行的节点
            for (NodeInstance instance : listener.getNodeInstances().values()) {
                if (nodeId.equals(instance.getRelyNode().id())){
                    listener.addAllCurrentNodes(Collections.singletonList(instance));
                    break;
                }
            }
            doRun0(listener);
            return listener;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public WorkflowInstanceListener run(Map<String, Object> param) {
        return run(new JSONObject(param));
    }

    @Override
    public WorkflowInstanceListener run(JSONObject param) {
        if (log.isInfoEnabled()) {
            log.info("开启一个工作流, formdata: {}", param);
        }
        WorkflowInstanceListener listener = WorkflowListenerFactory.createListener(param, this);
        List<NodeInstance> nodeInstances = NodeFactory.instanceNodeQueue(nodeQueue, listener);
        listener.refrushNodeInstances(nodeInstances);

        //开启任务
        configuration.getWriteDatabaseHandler().startWork(listener);

        //通知监听者一个实例开始了
        if (configuration.isAsynTracker()) {
            GlobalWorkflowManagementCenter.getInstance().asynInvokeTracker(() ->{
                for (WorkflowEventTracker tracker : configuration.getTrackerList(this)) {
                    tracker.workflowInstanceStart(this, listener);
                }
            });
        }else {
            for (WorkflowEventTracker tracker : configuration.getTrackerList(this)) {
                tracker.workflowInstanceStart(this, listener);
            }
        }

        doRun0(listener);
        return listener;
    }

    protected void doRun0(WorkflowInstanceListener listener){
        lock.lock();
        try {
            //当前要执行的所有节点
            List<NodeInstance> currentNodes = listener.getCurrentNodeInstances();
            Set<NodeInstance> blockingNodes = new HashSet<>();
            do {
                List<NodeInstance> nextNodes = new ArrayList<>();
                loopNode: for (NodeInstance currentNode : currentNodes) {

                    //invoke node runnable
                    for (WorkflowNodeRunnableHandler runnableHandler : configuration.getRunnableHandlers()) {
                        if (runnableHandler.support(listener, currentNode)){
                            try {
                                runnableHandler.invokeRunnable(listener, currentNode);
                            } catch (BlockingException e) {
                                blockingNodes.add(currentNode);
                                continue loopNode;
                            } catch (WorkflowNodeRunnableException e) {
                                //执行异常处理
                                CentralizedExceptionHandling.handlerException(e);
                                continue loopNode;
                            }
                            break;
                        }
                    }

                    if (!blockingNodes.contains(currentNode)){

                        //通知监听者一个任务结束了
                        if (configuration.isAsynTracker()) {
                            GlobalWorkflowManagementCenter.getInstance().asynInvokeTracker(() ->{
                                for (WorkflowEventTracker tracker : configuration.getTrackerList(this)) {
                                    tracker.taskFinish(this, currentNode, listener);
                                }
                            });
                        }else {
                            for (WorkflowEventTracker tracker : configuration.getTrackerList(this)) {
                                tracker.taskFinish(this, currentNode, listener);
                            }
                        }

                        //find no blocking node route path
                        for (WorkflowRouteResolver resolver : configuration.getRouteResolvers()) {
                            if (resolver.support(listener, currentNode)) {
                                try {
                                    nextNodes.addAll(resolver.flowTakeNodes(listener, currentNode));
                                } catch (ResolverConditionException e) {

                                    //handler resolver condition error
                                    CentralizedExceptionHandling.handlerException(e);
                                    continue loopNode;
                                }
                                break;
                            }
                        }
                    }
                }
                listener.addAllCurrentNodes(WorkUtils.merge(blockingNodes, nextNodes));
                currentNodes = nextNodes;
            }while (!currentNodes.isEmpty());

            if (!blockingNodes.isEmpty()){

                //write listener to database
                pauseWork(listener);
            }else {
                //finish work
                finishWork(listener);
            }
        }finally {
            lock.unlock();
        }
    }

    //暂停任务
    //首先将监听者存到缓存里
    //然后更新数据库里的信息
    protected void pauseWork(WorkflowInstanceListener listener){
        String id = listener.getInstance().id();
        Map<String, List<ScheduledNodeFutureWrapper>> futureCache = configuration.getScheduledTaskDispatcher().getFutureCache();
        List<String> scheduledNames = new ArrayList<>();
        List<NodeInstance> currentNodes = new ArrayList<>();
        if(futureCache.containsKey(id)){
            List<ScheduledNodeFutureWrapper> tasks = futureCache.get(id);
            for (ScheduledNodeFutureWrapper task : tasks) {
                scheduledNames.add(task.getNodeId());
            }
        }

        //在完成任务之前要去检查他还有没有定时任务存在, 或者是堵塞的节点存在
        listener.getNodeInstances().forEach((name, instance) -> {
            if (instance.hasBlocking() || scheduledNames.contains(instance.getRelyNode().id())) {

                currentNodes.add(instance);
            }
        });

        listener.addAllCurrentNodes(currentNodes);
        listenerCache.put(id, listener);
        configuration.getWriteDatabaseHandler().pauseTask(listener);
        if (log.isInfoEnabled()) {
            log.info("工作流挂起, 实例id:{}", id);
        }
    }

    protected void finishWork(WorkflowInstanceListener listener){
        String id = listener.getInstance().id();
        //判断工作流是否结束
        AtomicBoolean over = new AtomicBoolean(true);
        if (configuration.getScheduledTaskDispatcher().containTask(id)){
            //如果该实例还存在定时任务, 则此实例并没有结束
            over.set(false);
        }

        if (listener.hasBlockingNode()){
            //如果该实例还存在堵塞节点
            over.set(false);
        }

        if (over.get()){
            if (configuration.isAsynTracker()) {
                GlobalWorkflowManagementCenter.getInstance().asynInvokeTracker(() ->{
                    for (WorkflowEventTracker tracker : configuration.getTrackerList(this)) {
                        tracker.workflowInstanceFinish(this, listener);
                    }
                });
            }else {
                for (WorkflowEventTracker tracker : configuration.getTrackerList(this)) {
                    tracker.workflowInstanceFinish(this, listener);
                }
            }
            if (log.isInfoEnabled()) {
                log.info("工作流结束, 实例id:{}", id);
            }
            configuration.getWriteDatabaseHandler().finishTask(listener, null);
            listenerCache.remove(id);
        }else {
            pauseWork(listener);
        }
    }

    //取消一个实例
    public void cancelWork(String instanceId){
        WorkflowInstanceListener listener = findListener(instanceId);
        configuration.getScheduledTaskDispatcher().cancelTasks(instanceId);
        configuration.getWriteDatabaseHandler().finishTask(listener, "取消");
        if (log.isInfoEnabled()) {
            log.info("取消工作流实例, 实例id:{}", instanceId);
        }
    }

    //根据实例 id 找到其对应的监听者
    protected WorkflowInstanceListener findListener(String instanceId){
        WorkflowInstanceListener listener;
        if (listenerCache.containsKey(instanceId)){
            listener = listenerCache.get(instanceId);
        }else {
            listener = configuration.getWriteDatabaseHandler().getListener(instanceId, this);
            if (listener == null){
                throw new UnknowNodeInstanceException("找不到实例: " + instanceId);
            }
            NodeFactory.loadListenerInstance(nodeQueue, listener, configuration.getWriteDatabaseHandler().readDetailsEntry(instanceId));
        }
        return listener;
    }

    @Override
    public int size() {
        return nodeQueue.size();
    }

    @Override
    public void close() {
        //获取所有任务节点
        Map<String, List<ScheduledNodeFutureWrapper>> futureCache = configuration.getScheduledTaskDispatcher().getFutureCache();
        futureCache.forEach((instanceId, wrappers) ->{
            WorkflowInstanceListener listener = configuration.getWriteDatabaseHandler().getListener(instanceId, this);
            if (listener != null){
                WorkflowInstance instance = listener.getInstance();
                for (ScheduledNodeFutureWrapper wrapper : wrappers) {
                    log.info("序列化实例尚未结束的任务, 实例id:{}, 任务节点id:{}", instanceId, wrapper.getNodeId());
                    instance.putScheduledTime(wrapper.getNodeId(), wrapper.lifeTime());
                }
                configuration.getWriteDatabaseHandler().updateInstance(listener);
            }
        });
    }
}
