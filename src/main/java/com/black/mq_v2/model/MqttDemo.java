package com.black.mq_v2.model;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttDemo {

    /*
    QoS0，发送就不管了，最多一次；
    QoS1，发送之后依赖MQTT规范，是否启动重传消息，所以至少一次；
    QoS2，发送之后依赖MQTT消息机制，确保只有一次。
     */
    static void send() throws Throwable{
        MqttClient client = new MqttClient("broker", "clientid", new MemoryPersistence());
        // 连接参数
        MqttConnectOptions options = new MqttConnectOptions();
        // 设置用户名和密码
        options.setUserName("username");
        options.setPassword("password".toCharArray());
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        // 连接
        client.connect(options);
        // 创建消息并设置 QoS
        MqttMessage message = new MqttMessage("hello world".getBytes());
        message.setQos(0);
        // 发布消息
        client.publish("topic", message);
        // 关闭连接
        client.disconnect();
        // 关闭客户端
        client.close();
    }

    static void rece() throws Throwable{
        MqttClient client = new MqttClient("broker", "clientid", new MemoryPersistence());
        // 连接参数
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("username");
        options.setPassword("password".toCharArray());
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        // 设置回调
        client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
            }

            public void messageArrived(String topic, MqttMessage message) {
                System.out.println("topic: " + topic);
                System.out.println("Qos: " + message.getQos());
                System.out.println("message content: " + new String(message.getPayload()));

            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete---------" + token.isComplete());
            }

        });
        client.connect(options);
        client.subscribe("topic", 0);
    }

    public static void main(String[] args) {

    }



}
