package com.black.core.work.w1;

import com.black.core.spring.instance.InstanceFactory;
import com.black.core.work.w1.cache.CacheTask;
import com.black.core.work.w1.node.BooleanBranchNode;
import com.black.core.work.w1.node.BranchHandlerConditionType;
import com.black.core.work.w1.node.BranchTypeResolver;
import com.black.core.work.w1.time.ScheduledServiceDispatcher;


import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.black.core.work.w1.TaskFlowQueue.dynamicNode;

public class WorkFlowTemplateBuilder {


    final TaskFlowQueue taskFlowQueue;

    final InstanceFactory instanceFactory;
    HashSet<TaskMasterNode<Boolean>> hasJoin = new HashSet<>();

    HashSet<TaskMasterNode<Boolean>> dynamicNodeJoin = new HashSet<>();

    public WorkFlowTemplateBuilder(CacheTask cacheTask, List<TaskNodeResolver<Boolean>> resolvers, InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
        taskFlowQueue = new TaskFlowQueue(cacheTask, resolvers);
        taskFlowQueue.setScheduledTaskDispatcher(new ScheduledServiceDispatcher());
    }


    public void addDynamic(){
        taskFlowQueue.add(dynamicNode);
    }

    public IfConditionNode obtainIf(IfConditionType ifConditionType){
        IfConditionNode ifConditionNode = new IfConditionNode(ifConditionType);

        if (!hasJoin.contains(ifConditionNode)) {
            taskFlowQueue.add(ifConditionNode);
            hasJoin.add(ifConditionNode);
        }
        return ifConditionNode;
    }

    public BooleanBranchNode addBranch(BranchHandlerConditionType conditionType){
        BooleanBranchNode branchNode = new BooleanBranchNode(conditionType, instanceFactory.getInstance(BranchTypeResolver.class));
        taskFlowQueue.add(branchNode);
        return branchNode;
    }

    public BlockingHandlerNode obtainHandler(TaskTypeStaticFactory.BlockingDefaultProcessor handlerConditionType){
        return obtainHandler(handlerConditionType, -1, null);
    }

    public BlockingHandlerNode obtainHandler(TaskTypeStaticFactory.BlockingDefaultProcessor handlerConditionType, long time, TimeUnit unit){
        BlockingHandlerNode blockingHandlerNode = new BlockingHandlerNode((BlockingHandlerConditionType) tgp ->{
            handlerConditionType.handler(tgp);
            return UniqueKeyUtils.bool();
        }, unit, time);
        if (!hasJoin.contains(blockingHandlerNode)){
            taskFlowQueue.add(blockingHandlerNode);
            hasJoin.add(blockingHandlerNode);
        }
        return blockingHandlerNode;
    }

    public BlockingHandlerNode obtainHandler(BlockingHandlerConditionType handlerConditionType){
        return obtainHandler(handlerConditionType, -1, null);
    }

    public BlockingHandlerNode obtainHandler(BlockingHandlerConditionType handlerConditionType, long time, TimeUnit unit){
        BlockingHandlerNode blockingHandlerNode = new BlockingHandlerNode(handlerConditionType, unit, time);
        if (!hasJoin.contains(blockingHandlerNode)){
            taskFlowQueue.add(blockingHandlerNode);
            hasJoin.add(blockingHandlerNode);
        }
        return blockingHandlerNode;
    }

    public TaskMasterNode setTrue(IfConditionNode father, TaskType ifConditionType){
        return setIf(father, ifConditionType, true, -1, null);
    }

    public TaskMasterNode setTrue(IfConditionNode father, TaskType ifConditionType, long time, TimeUnit unit){
        return setIf(father, ifConditionType, true, time, unit);
    }

    public TaskMasterNode setIf(IfConditionNode father, TaskType ifConditionType, boolean left){
        return setIf(father, ifConditionType, left, -1, null);
    }

    public TaskMasterNode setIf(IfConditionNode father, TaskType ifConditionType, boolean left, long time, TimeUnit unit){
        TaskMasterNode<Boolean> node;
        if (ifConditionType instanceof  IfConditionType){
            node = new IfConditionNode(ifConditionType);
        }else {
            node = new BlockingHandlerNode(ifConditionType, unit, time);
        }
        if (left){
            father.setLeft(node);
        }else {
            father.setRight(node);
        }

        return node;
    }

    public TaskMasterNode setFalse(IfConditionNode father, TaskType ifConditionType){
        return setIf(father, ifConditionType, false, -1, null);
    }

    public TaskMasterNode setFalse(IfConditionNode father, TaskType ifConditionType, long time, TimeUnit unit){
        return setIf(father, ifConditionType, false, time, unit);
    }

    public TaskFlowQueue end(){
        return taskFlowQueue;
    }


}
