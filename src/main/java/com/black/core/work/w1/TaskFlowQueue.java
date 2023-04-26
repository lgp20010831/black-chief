package com.black.core.work.w1;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.work.w1.cache.CacheTask;
import com.black.core.work.w1.time.ScheduledFutureWrapper;
import com.black.core.work.w1.time.ScheduledTaskDispatcher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2  @SuppressWarnings("all")
public class TaskFlowQueue implements WorkFlow<Boolean> {

    //队列锁
    private final ReentrantLock lock = new ReentrantLock();

    @Setter private String alias;

    //动态 node
    public final static TaskMasterNode<Boolean> dynamicNode = new EmtryNode(null);

    //缓存工具
    private final CacheTask cacheTask;

    //逻辑处理器
    private final List<TaskNodeResolver<Boolean>> nodeResolvers;

    //定时任务调度器
    @Setter private ScheduledTaskDispatcher scheduledTaskDispatcher;

    //异常处理器
    @Setter private ProcessorError processorError;

    volatile TaskMasterNode<Boolean> head;

    volatile TaskMasterNode<Boolean> tail;

    ArrayList<TaskMasterNode<Boolean>> array;

    public TaskFlowQueue(CacheTask cacheTask, List<TaskNodeResolver<Boolean>> nodeResolvers) {
        this.cacheTask = cacheTask;
        this.nodeResolvers = nodeResolvers;
        array = new ArrayList<>();
    }

    //服务启动时触发, 主要是重新加载回未完成的任务
    public void init(){
        cacheTask.init(this);
    }


    public void serverBraking(TaskGlobalListener listener){
        int index = listener.currentIndex();
        TaskMasterNode<Boolean> node = queryByIndex(index);
        if (node != null){
            TaskType taskType = node.getTaskType();
            if (taskType.isServerBraking()){
                node.braking(listener);
            }
        }
    }

    public void submitTimerTask(TaskGlobalListener listener, TimeUnit unit, long life){
        scheduledTaskDispatcher.doDispatcher(listener, this, unit, life);
    }

    //完成一个任务
    public TaskGlobalListener complete(String taskId, Boolean result){
        TaskGlobalListener listener = cacheTask.readTaskListener(taskId);
        if (listener == null){
            throw new RuntimeException("任务不存在 " + taskId);
        }
        runAgain(listener, result);
        return listener;
    }

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public TaskGlobalListener run(Map<String, Object> param) {
        //创建一个监听器
        TaskGlobalListener listener = createLisener(param);
        doRun(listener, null);
        return listener;
    }

    //根据监听者 恢复任务进度
    //这个方法调用的时机
    //1. 可能是服务启动, 从缓存中读出来的任务, 继续执行
    //2.  可能是堵塞任务被挂起, 处理结束后回调, 重新执行
    public void runAgain(TaskGlobalListener listener, Boolean currentResult){
        ContinueTask continueTask = new ContinueTask(listener, currentResult);
        doRun(listener, continueTask);
    }

    protected void doRun(TaskGlobalListener listener, ContinueTask continueTask){
        lock.lock();
        try {
            Boolean currentResult = null;
            TaskMasterNode<Boolean> prevNode, currentNode;
            int currentIndex = listener.currentIndex();

            if (continueTask != null){
                currentResult = continueTask.getCurrentResult();
                if(scheduledTaskDispatcher != null){
                    scheduledTaskDispatcher.cancel(listener);
                }

                //如果结果不为空, 并且为 true, 则结束任务
                if (currentResult != null && currentResult){
                    finishTask(listener, true);
                    return;
                }

                currentNode = (prevNode = queryByIndex(currentIndex)).flowNextNode(currentResult, listener);
            }else {
                prevNode = currentIndex == 1  ?  null : queryByIndex(currentIndex - 1);
                currentNode = queryByIndex(currentIndex);
            }

            if(currentNode == null){
                finishTask(listener, false);
                return;
            }
            loop: do {
                //如果该节点是一个动态节点
                if (currentNode.equals(dynamicNode)){

                    //根据结果动态指定下一个节点
                    currentNode = prevNode.flowNextNode(currentResult, listener);
                    currentNode.setIndex(currentIndex + 1);
                }

                TaskType taskType = currentNode.getTaskType();
                try {
                    for (TaskNodeResolver<Boolean> nodeResolver : nodeResolvers) {
                        if (nodeResolver.support(taskType)) {
                            try {
                                currentResult =  nodeResolver.processor(taskType, listener, this);
                            } catch (WorkBreakException e) {

                                //任务中断
                                if (log.isInfoEnabled()) {
                                    log.info("task break");
                                }

                                if (taskType.isBlocking()){
                                    listener.setCuurentIndex(currentNode.index());
                                    //应该将任务进度缓存
                                    cacheTask.writeCache(listener);

                                    if (currentNode instanceof TimeOutNode){
                                        //判断任务存不存在截止时间
                                        TimeOutNode outNode = (TimeOutNode) currentNode;
                                        if (outNode.isDelayed() && scheduledTaskDispatcher != null){
                                            scheduledTaskDispatcher.doDispatcher(listener, this, outNode.getUnit(), outNode.getDeadline());
                                        }
                                    }
                                    return;
                                }else {
                                    throw new RuntimeException("任务异常中断", e);
                                }
                            }
                            break;
                        }
                    }
                }catch (Throwable ex){
                    CentralizedExceptionHandling.handlerException(ex);
                    boolean continueWork = false;
                    try {
                        if (processorError != null){
                            continueWork = processorError.handlerThrowable(ex, listener, this);
                        }
                    }catch (RuntimeException re){
                        if (log.isInfoEnabled()) {
                            log.info("处理异常时发生异常, 任务中断");
                        }
                        break loop;
                    }
                    if (!continueWork){
                        break loop;
                    }
                }finally {
                    //不管是 block 还是 正常进行 都会进行堵塞
                    listener.setCuurentIndex(currentNode.index());
                }
                prevNode = currentNode;
                currentNode = currentNode.getNextNode();
            }while (currentNode != null);

            //任务结束
            finishTask(listener, false);
        }finally {
            lock.unlock();
        }
    }

    //任务继续的封装类
    @AllArgsConstructor @Setter @Getter
    public static class ContinueTask{
        TaskGlobalListener listener;
        Boolean currentResult;
    }


    protected void finishTask(TaskGlobalListener listener, boolean result){
        if (log.isInfoEnabled()) {
            log.info("任务结束: {}", listener.getTaskId());
        }
        cacheTask.finishTask(listener, result);
    }

    public TaskMasterNode<Boolean> queryByIndex(int index){
        int i = index - 1;
        int size = array.size();
        if (i < 0 || i >= size){
            return null;
        }
        return array.get(i);
    }

    protected TaskGlobalListener createLisener(Map<String, Object> param){
        String taskId = UUID.randomUUID().toString();
        BooleanTaskListener listener = new BooleanTaskListener(size(), new DefaultMapTaskGlobalParam(param), taskId, alias);
        listener.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return listener;
    }

    public void add(TaskMasterNode<Boolean> node){
        lock.lock();
        try {
            if (head == null && tail == null){
                head = node;
                tail = node;
                node.setIndex(1);
                return;
            }
            TaskMasterNode<Boolean> tailNode = tail;
            tailNode.setNextNode(node);
            node.setIndex(array.size() + 1);
            tail = node;
        }finally {
            array.add(node);
            lock.unlock();
        }

    }

    @Override
    public int size() {
        return tail.index();
    }

    //服务停止
    public void close(){
        long serverStopTime = System.currentTimeMillis();
        //拿到所有的任务, 缓存到数据库
        Map<String, ScheduledFutureWrapper> futureCache = scheduledTaskDispatcher.getFutureCache();
        futureCache.forEach((id, w) ->{
            cacheTask.processorCloseServer(w.lifeTime(), w.startTime(), serverStopTime, id);
        });
    }
}
