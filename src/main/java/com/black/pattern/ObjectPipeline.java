package com.black.pattern;

public class ObjectPipeline extends Pipeline<ObjectNode, Object, Object>{

    public ObjectPipeline(){
        this(new ObjectNode(), new ObjectNode());
    }

    public ObjectPipeline(ObjectNode head, ObjectNode tail) {
        super(head, tail);
    }
}
