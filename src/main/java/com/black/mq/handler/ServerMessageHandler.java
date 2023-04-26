package com.black.mq.handler;

import com.black.mq.server.MQInputStream;
import com.black.mq.server.MQServer;
import com.black.mq.simple.DataType;
import com.black.mq.simple.Message;

public class ServerMessageHandler implements MessageHandler{

    private final MQServer server;

    public ServerMessageHandler(MQServer server) {
        this.server = server;
    }

    @Override
    public boolean support(int type) {
        return DataType.MESSAGE == type;
    }

    @Override
    public void handler(MQInputStream in, int type) throws Throwable {
        server.pushMessage((Message) in.readHexJavaObject());
    }
}
