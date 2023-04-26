package com.black.mq.simple;

import com.black.mq.handler.MessageHandler;

import java.util.concurrent.LinkedBlockingQueue;

public class MessageHandlerRegister {

    private final LinkedBlockingQueue<MessageHandler> messageHandlers = new LinkedBlockingQueue<>();

    public void registerHandler(MessageHandler handler){
        messageHandlers.add(handler);
    }

    public LinkedBlockingQueue<MessageHandler> getMessageHandlers() {
        return messageHandlers;
    }
}
