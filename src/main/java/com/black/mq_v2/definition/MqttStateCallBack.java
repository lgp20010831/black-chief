package com.black.mq_v2.definition;

public interface MqttStateCallBack {

    default void lostConnection(Throwable cause, MqttContext context){

    }

    default void messageArrived(String topic, Message message, MqttContext context) throws Throwable{

    }

}
