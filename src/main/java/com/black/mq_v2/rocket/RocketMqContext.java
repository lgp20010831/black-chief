package com.black.mq_v2.rocket;

import com.black.mq_v2.MQTTException;
import com.black.mq_v2.core.AbstractAsyncMqttContext;
import com.black.mq_v2.core.ByteMessage;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MessageSendCallback;
import com.black.mq_v2.definition.MqttStateCallBack;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;

public class RocketMqContext extends AbstractAsyncMqttContext {

    public static String GROUP_NAME = "group";
    protected final DefaultMQProducer producer;

    protected final DefaultMQPushConsumer consumer;

    public RocketMqContext(){
        producer = new DefaultMQProducer(GROUP_NAME);
        consumer = new DefaultMQPushConsumer(GROUP_NAME);
    }

    @Override
    protected Object buildSource() {
        return producer;
    }

    @Override
    protected void doConnect() throws Throwable {
        String serverAddr = serverHost + ":" + serverPort;
        producer.setNamesrvAddr(serverAddr);
        consumer.setNamesrvAddr(serverAddr);
        producer.start();
        consumer.start();
    }

    @Override
    protected void close0() {
        producer.shutdown();
        consumer.shutdown();
    }

    @Override
    public void subscribe(String... topics) {
        for (String topic : topics) {
            try {
                consumer.subscribe(topic, "*");
            } catch (MQClientException e) {
                throw new MQTTException(e);
            }
        }
    }

    @Override
    public void sendMessage(Message message, MessageSendCallback callback) {
        org.apache.rocketmq.common.message.Message msg =
                new org.apache.rocketmq.common.message.Message(message.getTopic(), message.getBody());
        try {
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    if (callback != null){
                        try {
                            callback.onSuccess(message);
                        } catch (Throwable e) {
                            log.error(e);
                        }
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                     if (callback != null){
                         callback.onFair(throwable, message);
                     }
                }
            });
        } catch (Throwable e) {
            throw new MQTTException(e);
        }
    }

    @Override
    public void setStateCallback(MqttStateCallBack callback) {
        RocketMqContext context = this;
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                if (callback != null){
                    for (MessageExt ext : list) {
                        String topic = ext.getTopic();
                        log.debug("[{}] -- arrived message topic: {}", topic);
                        try {
                            callback.messageArrived(topic, castMessageExtToMessage(ext), context);
                        } catch (Throwable e) {
                            log.error(e);
                            log.error("[{}] -- handler message: {} error", topic);
                        }
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
    }

    protected Message castMessageExtToMessage(MessageExt ext){
        ByteMessage message = new ByteMessage();
        message.setTopic(ext.getTopic());
        message.setBody(ext.getBody());
        return message;
    }

    @Override
    public void importProperties(Map<String, String> properties) {

    }
}
