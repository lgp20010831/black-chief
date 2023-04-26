package com.black.mq.handler;

import com.black.mq.client.MQClient;
import com.black.mq.server.MQInputStream;
import com.black.mq.simple.DataType;
import lombok.NonNull;

public class ActiveFinishHandler implements MessageHandler{

    private final MQClient client;

    public ActiveFinishHandler(@NonNull MQClient client) {
        this.client = client;
    }

    @Override
    public boolean support(int type) {
        return type == DataType.FINISH;
    }

    @Override
    public void handler(MQInputStream in, int type) throws Throwable {
        client.finishActive();
    }
}
