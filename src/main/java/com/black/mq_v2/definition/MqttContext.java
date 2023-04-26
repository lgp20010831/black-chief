package com.black.mq_v2.definition;

import com.black.core.log.IoLog;

import java.util.Map;

public interface MqttContext {

    default void setServerHostAndPort(int port){
        setServerHostAndPort("0.0.0.0", port);
    }

    void setServerHostAndPort(String host, int port);

    void setServerUrl(String url);

    void setUserName(String userName);

    void createSource();

    void setPassword(String password);

    IoLog getLog();

    String getName();

    void setName(String name);

    void connect();

    void subscribe(String... topics);

    void setAsync(boolean async);

    default void sendAsyncMessage(Message message){
        sendAsyncMessage(message, null);
    }

    void sendAsyncMessage(Message message, MessageSendCallback callback);

    default void sendMessage(Message message){
        sendMessage(message, null);
    }

    void sendMessage(Message message, MessageSendCallback callback);

    void setStateCallback(MqttStateCallBack callback);

    Object source();

    void importProperties(Map<String, String> properties);

    void close();


}
