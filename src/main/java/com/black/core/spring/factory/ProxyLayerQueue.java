package com.black.core.spring.factory;

import com.black.utils.PostHandlerQueue;
import lombok.NonNull;

import java.util.Collection;

public final class ProxyLayerQueue extends PostHandlerQueue<AgentLayer> {

    public ProxyLayerQueue(AgentLayer fixLastLayer){
        queue.addLast(fixLastLayer);
    }

    @Override
    public void addFirst(AgentLayer argParser) {
        if (queue.contains(argParser)){
            return;
        }
        super.addFirst(argParser);
    }

    @Override
    public void add(int index, AgentLayer element) {
        if (queue.contains(element)){
            return;
        }
        final int size = queue.size();
        if (index == -1){
            index = size >= 2 ? size - 2 : 0;
        }else if (index >= 2 && index == queue.size() - 1){
            --index;
        }
        super.add(index, element);
    }

    @Override
    public boolean add(AgentLayer agentLayer) {
        if (queue.contains(agentLayer)){
            return false;
        }
        add(queue.size() - 1, agentLayer);
        return true;
    }

    @Override
    public AgentLayer set(int index, AgentLayer element) {
        add(index, element);
        return element;
    }

    @Override
    public boolean addAll(Collection<? extends AgentLayer> c) {
        throw new UnsupportedOperationException("Please add one by one");
    }

    @Override
    public boolean addAll(int index, Collection<? extends AgentLayer> c) {
        throw new UnsupportedOperationException("Please add one by one");
    }

    public Object invoke(@NonNull AgentObject agentObject) throws Throwable {
        AgentLayer agentLayer = queue.getFirst();
        return agentLayer.proxy(agentObject);
    }
}
