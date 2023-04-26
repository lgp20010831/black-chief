package com.black.mq.simple;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ActivationPackage implements Serializable {

    private final Set<String> topics;

    public ActivationPackage() {
        topics = new HashSet<>();
    }

    public void registerTopic(String topic){
        topics.add(topic);
    }

    public void remove(String topic){
        topics.remove(topic);
    }

    public void clear(){
        topics.clear();
    }

    public Set<String> getTopics() {
        return topics;
    }
}
