package com.black.mq_v2.proxy.aop;

import com.black.mq_v2.definition.MqttContext;

public class MqttMethodBodyAssort {

    private final MqttContext context;

    public MqttMethodBodyAssort(MqttContext context) {
        this.context = context;
    }
}
