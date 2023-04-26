package com.black.core.work.w2.connect.node;



import com.black.core.util.StringUtils;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.entry.WorkflowNodeInstanceDetailsEntry;
import com.black.core.work.utils.WorkUtils;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeFactory {

    private static final Map<String, Map<String, AtomicInteger>> references = new HashMap<>();

    public static WorkflowNode create(WorkflowNodeDefinitional definitional, Object... args){
        Class<? extends WorkflowNode> nodeType = definitional.nodeType();
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            types[i] = arg == null ? Object.class : arg.getClass();
        }
        Constructor<?> targetConstructor = null;
        find: for (Constructor<?> constructor : nodeType.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == types.length){
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    Class<?> type = types[i];
                    if (!parameterType.isAssignableFrom(type)){
                        continue find;
                    }
                }
                targetConstructor = constructor;
                break;
            }
        }

        if (targetConstructor == null){
            throw new RuntimeException("找不到节点构造器");
        }
        try {
            return (WorkflowNode) targetConstructor.newInstance(args);
        }catch (Exception e){
            throw new RuntimeException("创建节点失败", e);
        }
    }

    public static String createDefaultName(String moduleName, String workflowName){
        Map<String, AtomicInteger> absent = references.computeIfAbsent(workflowName, n -> new HashMap<>());
        AtomicInteger integer = absent.computeIfAbsent(moduleName, n -> new AtomicInteger(0));
        integer.incrementAndGet();
        return StringUtils.linkStr(workflowName, moduleName, integer.toString());
    }


    public static List<NodeInstance> instanceNodeQueue(List<WorkflowNode> workflowNodes, WorkflowInstanceListener listener){
        List<NodeInstance> nodeInstances = new ArrayList<>();
        for (WorkflowNode node : workflowNodes) {
            Collection<NodeInstance> instances = node.getInstances();
            for (NodeInstance instance : instances) {
                DefaultNodeInstance nodeInstance = (DefaultNodeInstance) instance;
                nodeInstance.setInstance(listener.getInstance());
            }
            nodeInstances.addAll(instances);
        }
        return nodeInstances;
    }


    public static WorkflowInstanceListener loadListenerInstance(List<WorkflowNode> workflowNodes,
                                                                WorkflowInstanceListener listener,
                                                                List<WorkflowNodeInstanceDetailsEntry> detailsEntries){
        List<NodeInstance> nodeInstances = instanceNodeQueue(workflowNodes, listener);
        Map<Integer, WorkflowNodeInstanceDetailsEntry> entryMap = new HashMap<>();
        for (WorkflowNodeInstanceDetailsEntry detailsEntry : detailsEntries) {
            entryMap.put(detailsEntry.getLevel(), detailsEntry);
        }
        for (NodeInstance nodeInstance : nodeInstances) {
            int index = nodeInstance.getRelyNode().index();
            WorkflowNodeInstanceDetailsEntry detailsEntry = entryMap.get(index);
            if (detailsEntry != null){
                WorkUtils.load(detailsEntry, nodeInstance);
            }
        }
        listener.refrushNodeInstances(nodeInstances);
        return listener;
    }

}
