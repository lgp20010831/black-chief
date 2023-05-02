package com.black.core.data;

import com.black.core.chain.*;
import com.black.core.data.annotation.Consumer;
import com.black.core.data.annotation.EnabledDataTransferStation;
import com.black.core.ill.GlobalThrowableCentralizedHandling;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashSet;


@LoadSort(46)
@ChainClient(TransferStationComponent.class)
@LazyLoading(EnabledDataTransferStation.class)
public class TransferStationComponent implements OpenComponent, CollectedCilent, ChainPremise {

    private final Collection<Object> consumers = new HashSet<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        for (Object consumer : consumers) {
            Consumer annotation = AnnotationUtils.getAnnotation(BeanUtil.getPrimordialClass(consumer), Consumer.class);
            for (String name : annotation.serverNames()) {
                try {

                    TransferStationManager.registerConsumer((DataConsumer) consumer, name);
                }catch (RuntimeException e){
                    GlobalThrowableCentralizedHandling.resolveThrowable(e);
                }
            }
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("consumer", stu ->{
            return DataConsumer.class.isAssignableFrom(stu) && BeanUtil.isSolidClass(stu)
                    && AnnotationUtils.getAnnotation(stu, Consumer.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("consumer".equals(resultBody.getAlias())){
            consumers.addAll(resultBody.getCollectSource());
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledDataTransferStation.class);
    }
}
