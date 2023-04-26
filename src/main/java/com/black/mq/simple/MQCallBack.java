package com.black.mq.simple;

import com.black.mq.client.MQClient;

public interface MQCallBack {

    void messageReceived(String topic, Message message) throws Throwable;

    default void connectLost(MQClient client){

    }

    default void connectComplete(MQClient client){

    }
}
