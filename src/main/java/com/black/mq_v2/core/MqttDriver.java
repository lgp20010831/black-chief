package com.black.mq_v2.core;

public enum MqttDriver {

    MQTT("com.example.mq_v2.mqtt.MQttContext"),
    KAFKA("com.example.mq_v2.kafka.KafkaContext"),
    ROCKET_MQ("com.example.mq_v2.rocket.RocketMqContext"),

    CHIEF("com.example.mq_v2.chief.ChiefMqttContext");

    final String path;

    MqttDriver(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
