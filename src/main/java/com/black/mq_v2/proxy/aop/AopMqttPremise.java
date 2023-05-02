package com.black.mq_v2.proxy.aop;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.Premise;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.mq_v2.annotation.EnabledMqttExt;

public class AopMqttPremise implements Premise {
    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        return ChiefApplicationRunner.isPertain(EnabledMqttExt.class);
    }
}
