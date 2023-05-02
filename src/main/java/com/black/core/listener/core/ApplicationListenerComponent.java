package com.black.core.listener.core;

import com.black.listener.ApplicationDispatcher;
import com.black.listener.inter.Listener;
import com.black.core.chain.*;
import com.black.core.listener.annotation.ApplicationListener;
import com.black.core.listener.annotation.EnabledApplicationListenerOccur;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;

@Log4j2
@ChainClient(ApplicationListenerComponent.class)
@LazyLoading(EnabledApplicationListenerOccur.class)
public class ApplicationListenerComponent implements OpenComponent, CollectedCilent, ChainPremise {

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
       log.info("type matcher: [{}], mark matcher: [{}]",
               ApplicationDispatcher.getTypeMatchingListenerCache().size(),
               ApplicationDispatcher.getMarkMatchingListenerCache().size());
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("lis", sre -> {
            return Listener.class.isAssignableFrom(sre) && BeanUtil.isSolidClass(sre) && sre.isAnnotationPresent(ApplicationListener.class);
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals("lis")){
            Collection<Object> collectSource = resultBody.getCollectSource();
            for (Object obj : collectSource) {
                ApplicationDispatcher.registerListener((Listener<?>) obj);
            }
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledApplicationListenerOccur.class);
    }
}
