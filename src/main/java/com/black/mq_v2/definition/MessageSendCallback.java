package com.black.mq_v2.definition;

public interface MessageSendCallback {

    void onSuccess(Message message) throws Throwable;

    void onFair(Throwable e, Message message);
}
