package com.black.core.event;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.cache.GlobalAopDispatcherCache;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.InstanceClientAdapter;

import java.util.Collection;

public class EventAdapter implements InstanceClientAdapter {

    @Override
    public CollectedCilent getClient() {
        AbstractAopTaskQueueAdapter adapter = GlobalAopDispatcherCache.getAbstractAopTaskQueueAdapter();
        if (adapter == null){
            return null;
        }
        Collection<AopTaskManagerHybrid> hybrids = adapter.getHybrids();
        for (AopTaskManagerHybrid hybrid : hybrids) {
            if (hybrid instanceof AnnotationEventAutoDispenser){
                return  (AnnotationEventAutoDispenser) hybrid;
            }
        }
        return null;
    }
}
