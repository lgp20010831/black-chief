package com.black.mq.handler;

import com.black.mq.server.MQInputStream;
import com.black.mq.server.MQServer;
import com.black.mq.simple.DataType;

public class ComfirmHandler implements MessageHandler{

    private final MQServer server;

    public ComfirmHandler(MQServer server) {
        this.server = server;
    }

    @Override
    public boolean support(int type) {
        return type == DataType.MESSAGE_COMFIRM;
    }

    @Override
    public void handler(MQInputStream in, int type) throws Throwable {
        String messageId = in.readHexObjectString(in.available());
        server.confirmMessage(messageId);
    }
}
