package com.black.mq_v2.definition;

public interface Message {

    void setTopic(String topic);

    String getTopic();

    void setBody(byte[] buf);

    byte[] getBody();

}
