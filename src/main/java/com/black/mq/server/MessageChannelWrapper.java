package com.black.mq.server;

import com.black.mq.simple.Message;
import lombok.Getter;

@Getter
public class MessageChannelWrapper {

    private final Message message;

    private final MQOutputStream out;

    public MessageChannelWrapper(Message message, MQOutputStream out) {
        this.message = message;
        this.out = out;
    }
}
