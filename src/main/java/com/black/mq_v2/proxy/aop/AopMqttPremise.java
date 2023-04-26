package com.black.mq_v2.proxy.aop;

import com.black.mq_v2.annotation.EnabledMqttExt;
import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.Premise;
import com.black.core.spring.ChiefApplicationRunner;

public class AopMqttPremise implements Premise {
    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && mainClass.isAnnotationPresent(EnabledMqttExt.class);
    }
}
