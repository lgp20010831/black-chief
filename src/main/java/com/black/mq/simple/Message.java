package com.black.mq.simple;

import java.io.Serializable;
import java.util.Set;

public interface Message extends Serializable {

    default Object getContent(){
        return content();
    }

    Object content();

    default String getMessageId(){
        return messageId();
    }

    String messageId();

    default int getLevel(){
        return level();
    }

    int level();

    default Set<String> getTopics(){
        return topics();
    }

    Set<String> topics();

    default void addTopics(String... topics){
        if (topics != null){
            for (String topic : topics) {
                addTopic(topic);
            }
        }
    }

    void addTopic(String topic);
}
