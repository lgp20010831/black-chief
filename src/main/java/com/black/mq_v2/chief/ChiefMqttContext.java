package com.black.mq_v2.chief;

import com.black.mq.client.MQClient;
import com.black.mq.simple.MQCallBack;
import com.black.mq_v2.MQTTException;
import com.black.mq_v2.core.AbstractAsyncMqttContext;
import com.black.mq_v2.core.ByteMessage;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MessageSendCallback;
import com.black.mq_v2.definition.MqttStateCallBack;
import com.black.utils.CollectionUtils;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.util.Map;

public class ChiefMqttContext extends AbstractAsyncMqttContext {


    private MQClient client;

    @Override
    protected Object buildSource() {
        if (client == null){
            client = new MQClient(serverHost, serverPort);
        }
        return client;
    }

    @Override
    protected void doConnect() throws Throwable {
        try {
            client.connectAndActive();
        } catch (IOException e) {
            throw new MQTTException(e);
        }
    }

    @Override
    protected void close0() {
        if (client != null){
            try {
                client.close();
            } catch (IOException e) {
                throw new MQTTException(e);
            }
        }
    }

    @Override
    public void subscribe(String... topics) {
        client.registerTopics(topics);
    }

    @Override
    public void sendMessage(Message message, MessageSendCallback callback) {
        try {
            client.sendMessage(message.getBody(), message.getTopic());
            if(callback != null){
                try {
                    callback.onSuccess(message);
                } catch (Throwable e) {
                    log.error(e);
                }
            }
        } catch (IOException e) {
            if (callback != null){
                callback.onFair(e, message);
            }
        }
    }

    private Message castMqMsgToMessage(com.black.mq.simple.Message message){
        ByteMessage byteMessage = new ByteMessage();
        byteMessage.setBody(IoUtils.getBytes(message.content(), true));
        byteMessage.setTopic(CollectionUtils.firstElement(message.topics()));
        return byteMessage;
    }

    @Override
    public void setStateCallback(MqttStateCallBack callback) {
        ChiefMqttContext context = this;
        if (client != null){
            client.setCallBack(new MQCallBack() {
                @Override
                public void messageReceived(String topic, com.black.mq.simple.Message message) throws Throwable {
                    if (callback != null){
                        callback.messageArrived(topic, castMqMsgToMessage(message), context);
                    }
                }

                @Override
                public void connectComplete(MQClient client) {
                    MQCallBack.super.connectComplete(client);
                }

                @Override
                public void connectLost(MQClient client) {
                    if (callback != null){
                        callback.lostConnection(null, context);
                    }
                }
            });
        }
    }

    @Override
    public void importProperties(Map<String, String> properties) {

    }
}
