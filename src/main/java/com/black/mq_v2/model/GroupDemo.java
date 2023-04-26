package com.black.mq_v2.model;

import com.black.mq_v2.annotation.MqttArrived;
import com.black.mq_v2.definition.MqttContext;
import com.black.mq_v2.kafka.KafkaContext;

public class GroupDemo {

    public static void main(String[] args) {
        MqttContext context = new KafkaContext();
    }

    @MqttArrived
    void get(){

    }
}
