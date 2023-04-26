package com.black.mq_v2.mqtt;

import com.black.mq_v2.MQTTException;
import com.black.mq_v2.core.AbstractAsyncMqttContext;
import com.black.mq_v2.core.ByteMessage;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MessageSendCallback;
import com.black.mq_v2.definition.MqttStateCallBack;
import com.black.utils.IdUtils;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;

@Getter @Setter
public class MQttContext extends AbstractAsyncMqttContext {

    private final String clientId;

    private MqttClient client;

    private final MqttConnectOptions options;

    public static final int qos = 2;

    private final Set<String> topics = new HashSet<>();

    public MQttContext() {
        clientId = IdUtils.createShort8Id();
        options = new MqttConnectOptions();
    }

    @Override
    protected Object buildSource() {
        if (client != null){
            return client;
        }
        MemoryPersistence persistence = new MemoryPersistence();
        String url = getServerUrl();
        try {
            if (url == null){
                url = "tcp://" + serverHost + ":" + serverPort;

            }
            log.debug("[{}] -- connect mqtt url: {}", url);
            client = new MqttClient(url, clientId, persistence);
            options.setUserName(userName);
            options.setPassword(getPassword().toCharArray());
        }catch (Throwable e){
            throw new MQTTException(e);
        }
        return client;
    }

    @Override
    protected void doConnect() throws Throwable {
        client.connect(options);
        for (String topic : topics) {
            subscribe0(topic);
        }
    }

    @Override
    protected void close0() {
        try {
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            throw new MQTTException(e);
        }
    }

    @Override
    public void subscribe(String... topics) {
        if (client == null){
            this.topics.addAll(Arrays.asList(topics));
        }else {
            for (String topic : topics) {
                subscribe0(topic);
            }
        }
    }

    private void subscribe0(String topic){
        try {
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            throw new MQTTException(e);
        }
    }

    @Override
    public void sendMessage(Message message, MessageSendCallback callback) {
        String topic = message.getTopic();
        MqttMessage mqttMessage = new MqttMessage(message.getBody());
        mqttMessage.setQos(qos);
        if (client != null){
            try {
                client.publish(topic, mqttMessage);
                if (callback != null){
                    try {
                        callback.onSuccess(message);
                    } catch (Throwable e) {
                        log.error(e);
                    }
                }
            } catch (MqttException e) {
                if (callback != null){
                    callback.onFair(e, message);
                }
            }
        }
    }

    @Override
    public void setStateCallback(MqttStateCallBack callback) {
        MQttContext context = this;
        if (client != null){
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    if (callback != null){
                        callback.lostConnection(cause, context);
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(callback != null){
                        try {
                            callback.messageArrived(topic, castMqttMessageToMessage(topic, message), context);
                        } catch (Throwable e) {
                            log.error(e);
                            log.error("[{}] -- handler message: {} error", topic);
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }
    }

    private Message castMqttMessageToMessage(String topic, MqttMessage message){
        ByteMessage byteMessage = new ByteMessage();
        byteMessage.setBody(message.getPayload());
        byteMessage.setTopic(topic);
        return byteMessage;
    }

    @Override
    public void importProperties(Map<String, String> properties) {

    }
}
