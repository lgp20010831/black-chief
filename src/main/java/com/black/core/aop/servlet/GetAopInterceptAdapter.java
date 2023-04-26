package com.black.core.aop.servlet;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.cache.GlobalAopDispatcherCache;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.InstanceClientAdapter;
import com.black.core.spring.instance.InstanceFactory;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;

public class GetAopInterceptAdapter implements InstanceClientAdapter {

    private final InstanceFactory instanceFactory;
    private final BeanFactory beanFactory;

    public GetAopInterceptAdapter(InstanceFactory instanceFactory, BeanFactory beanFactory) {
        this.instanceFactory = instanceFactory;
        this.beanFactory = beanFactory;
    }

    @Override
    public CollectedCilent getClient() {
        AbstractAopTaskQueueAdapter adapter = GlobalAopDispatcherCache.getAbstractAopTaskQueueAdapter();
        if (adapter == null){
            return null;
        }
        Collection<AopTaskManagerHybrid> hybrids = adapter.getHybrids();
        for (AopTaskManagerHybrid hybrid : hybrids) {
            if (hybrid instanceof AopEnhanceControllerHybrid){
                AopEnhanceControllerHybrid controllerHybrid = (AopEnhanceControllerHybrid) hybrid;
                return (CollectedCilent) controllerHybrid.obtainAopTaskIntercept();
            }
        }
        return null;
    }
}
