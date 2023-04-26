package com.black.core.work.w2.connect.time;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;
import lombok.extern.log4j.Log4j2;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class DefaultScheduledServiceDispatcher implements WorkflowScheduledTaskDispatcher{

    private final ScheduledExecutorService executorService;

    // key = instance id
    private final Map<String, List<ScheduledNodeFutureWrapper>> nodeTaskCache = new ConcurrentHashMap<>();

    public DefaultScheduledServiceDispatcher(ScheduledExecutorService executorService){
        this.executorService = executorService;
    }

    @Override
    public void doDispatcher(WorkflowInstanceListener listener, String nodeId, WorkflowProcessing queue, TimeUnit unit, long time) {
        String id = listener.getInstance().id();
        List<ScheduledNodeFutureWrapper> wrappers = nodeTaskCache.computeIfAbsent(id, i -> new ArrayList<>());

        //提交任务
        ScheduledFuture<?> future = executorService.schedule(new FlowTask(nodeId, listener, queue, this), time, unit);
        if (log.isInfoEnabled()) {
            log.info("提交一个定时任务, 实例id:{}", id);
        }
        DefaultNodeFutureWrapper futureWrapper = new DefaultNodeFutureWrapper(future, nodeId, time, System.currentTimeMillis());
        wrappers.add(futureWrapper);
    }

    @Override
    public void cancel(WorkflowInstanceListener listener, String nodeId) {
        String id = listener.getInstance().id();
        List<ScheduledNodeFutureWrapper> wrappers = nodeTaskCache.get(id);
        if (wrappers != null){
            Iterator<ScheduledNodeFutureWrapper> iterator = wrappers.iterator();
            while (iterator.hasNext()) {
                ScheduledNodeFutureWrapper wrapper = iterator.next();
                if (nodeId.equals(wrapper.getNodeId())) {
                    if (log.isInfoEnabled()) {
                        log.info("取消一个定时任务, 实例id:{}, 节点id:{}", id, nodeId);
                    }
                    wrapper.cancel();
                    iterator.remove();
                    break;
                }
            }
            if (wrappers.isEmpty()){
                nodeTaskCache.remove(id);
            }
        }
    }

    @Override
    public void callBackDispatcher(WorkflowInstanceListener listener, WorkflowProcessing queue, String nodeId) {
        String id = listener.getInstance().id();
        List<ScheduledNodeFutureWrapper> wrappers = nodeTaskCache.get(id);
        if (wrappers != null){
            wrappers.removeIf(w -> nodeId.equals(w.getNodeId()));
        }
    }

    @Override
    public Map<String, List<ScheduledNodeFutureWrapper>> getFutureCache() {
        return nodeTaskCache;
    }

    @Override
    public boolean containTask(String instanceId) {
        return getFutureCache().containsKey(instanceId);
    }

    @Override
    public List<ScheduledNodeFutureWrapper> getFutureTasksByInstanceId(String instanceId) {
        return getFutureCache().get(instanceId);
    }

    @Override
    public void cancelTasks(String instanceId) {
        List<ScheduledNodeFutureWrapper> futureWrappers = getFutureTasksByInstanceId(instanceId);
        if (futureWrappers != null){
            if (futureWrappers.isEmpty()) {
                getFutureCache().remove(instanceId);
            }else {
                Iterator<ScheduledNodeFutureWrapper> iterator = futureWrappers.iterator();
                while (iterator.hasNext()) {
                    ScheduledNodeFutureWrapper wrapper = iterator.next();
                    wrapper.cancel();
                    iterator.remove();
                }
            }
        }
    }
}
