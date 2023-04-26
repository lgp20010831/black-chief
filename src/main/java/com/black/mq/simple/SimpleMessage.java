package com.black.mq.simple;

import com.black.JsonBean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimpleMessage extends JsonBean implements Message{

    private final Object content;

    private int level;

    private final String messageId;

    private final Set<String> topics;

    public SimpleMessage(Object content){
        this(content, 1);
    }

    public SimpleMessage(Object content, int level) {
        this.content = content;
        if (level < 0 || level > 1){
            throw new IllegalArgumentException("ill message level");
        }
        this.level = level;
        messageId = UUID.randomUUID().toString();
        topics = new HashSet<>();
    }

    @Override
    public Object content() {
        return content;
    }

    @Override
    public String messageId() {
        return messageId;
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public Set<String> topics() {
        return topics;
    }

    @Override
    public void addTopic(String topic) {
        topics.add(topic);
    }


}
