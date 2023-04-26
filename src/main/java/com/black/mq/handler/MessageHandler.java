package com.black.mq.handler;

import com.black.mq.server.MQInputStream;

public interface MessageHandler {

    //第一个字节标头
    boolean support(int type);

    void handler(MQInputStream in, int type) throws Throwable;
}
