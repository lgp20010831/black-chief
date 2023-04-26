package com.black.core.work.w1.time;

import com.black.core.work.w1.TaskFlowQueue;
import com.black.core.work.w1.TaskGlobalListener;

import java.util.Map;
import java.util.concurrent.*;

public class ScheduledServiceDispatcher implements ScheduledTaskDispatcher{

    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<String, ScheduledFutureWrapper> futureCache = new ConcurrentHashMap<>();

    public ScheduledServiceDispatcher() {
        //构造一个任务线程池
        scheduledExecutorService = ScheduledFuturePoolManager.getScheduledService();
    }

    @Override
    public void doDispatcher(TaskGlobalListener listener, TaskFlowQueue queue, TimeUnit unit, long time) {
        TimerTask timerTask = new TimerTask(listener, queue, this);
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(timerTask, time, unit);
        DefaultScheduleFutureWrapper wrapper = new DefaultScheduleFutureWrapper(scheduledFuture, unit.toMillis(time), System.currentTimeMillis());
        futureCache.put(listener.getTaskId(), wrapper);
    }

    @Override
    public void cancel(TaskGlobalListener listener) {
        ScheduledFutureWrapper futureWrapper = futureCache.get(listener.getTaskId());
        if (futureWrapper != null){
            ScheduledFuture<?> future = futureWrapper.future();
            future.cancel(true);
            futureCache.remove(listener.getTaskId());
        }
    }

    @Override
    public void callBackDispatcher(TaskGlobalListener listener, TaskFlowQueue queue) {
        futureCache.remove(listener.getTaskId());
    }

    @Override
    public Map<String, ScheduledFutureWrapper> getFutureCache() {
        return futureCache;
    }
}
