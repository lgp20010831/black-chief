package com.black.mq.handler;

import com.black.mq.client.MQClient;
import com.black.mq.server.MQInputStream;
import com.black.mq.simple.DataType;
import com.black.mq.simple.Message;

public class ClientMessageHandler implements MessageHandler{

    private final MQClient client;

    public ClientMessageHandler(MQClient client) {
        this.client = client;
    }

    @Override
    public boolean support(int type) {
        return type == DataType.MESSAGE;
    }

    @Override
    public void handler(MQInputStream in, int type) throws Throwable {
        client.dispatchMessage((Message) in.readHexJavaObject());
    }
}
