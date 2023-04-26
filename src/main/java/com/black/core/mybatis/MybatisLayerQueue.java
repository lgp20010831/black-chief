package com.black.core.mybatis;


import com.black.utils.PostHandlerQueue;
import lombok.NonNull;

import java.util.Collection;

public class MybatisLayerQueue extends PostHandlerQueue<LayerWrapper> {


    public MybatisLayerQueue(LayerWrapper fixLastLayer){
        queue.addLast(fixLastLayer);
    }

    @Override
    public void addFirst(LayerWrapper argParser) {
        if (queue.contains(argParser)){
            return;
        }
        super.addFirst(argParser);
    }

    @Override
    public void add(int index, LayerWrapper element) {
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
    public boolean add(LayerWrapper agentLayer) {
        if (queue.contains(agentLayer)){
            return false;
        }
        add(queue.size() - 1, agentLayer);
        return true;
    }

    @Override
    public LayerWrapper set(int index, LayerWrapper element) {
        add(index, element);
        return element;
    }

    @Override
    public boolean addAll(Collection<? extends LayerWrapper> c) {
        throw new UnsupportedOperationException("Please add one by one");
    }

    @Override
    public boolean addAll(int index, Collection<? extends LayerWrapper> c) {
        throw new UnsupportedOperationException("Please add one by one");
    }

    public Object invoke(@NonNull MybatisLayerObject mybatisLayer){
        LayerWrapper agentLayer = queue.getFirst();
        return agentLayer.getTarget().doIntercept(mybatisLayer);
    }

}
