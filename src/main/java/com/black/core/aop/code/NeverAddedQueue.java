package com.black.core.aop.code;


import com.black.utils.PostHandlerQueue;

import java.util.Collection;

public class NeverAddedQueue<Q> extends PostHandlerQueue<Q> {

    public NeverAddedQueue(Q q){
        queue.addLast(q);
    }

    @Override
    public void addFirst(Q element) {
        if (queue.contains(element)){
            return;
        }
        super.addFirst(element);
    }

    @Override
    public void add(int index, Q element) {
        if (queue.contains(element)){
            return;
        }
        final int size = queue.size();
        if (index < 0){
            index = size >= 2 ? size - 2 : 0;
        }else if (index >= 2 && index == queue.size()){
            --index;
        }else if (index == size){
            index = index - 2;
            if (index < 0){
                index = 0;
            }
        }else if (index > size){
            index = size - 1;
        }
        super.add(index, element);
    }

    @Override
    public boolean add(Q agentLayer) {
        if (queue.contains(agentLayer)){
            return false;
        }
        add(queue.size() - 1, agentLayer);
        return true;
    }

    @Override
    public Q set(int index, Q element) {
        add(index, element);
        return element;
    }

    @Override
    public boolean addAll(Collection<? extends Q> c) {
        throw new UnsupportedOperationException("Please add one by one");
    }

    @Override
    public boolean addAll(int index, Collection<? extends Q> c) {
        throw new UnsupportedOperationException("Please add one by one");
    }
}
