package com.black.mq_v2.core;

import com.black.mq_v2.definition.Message;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ByteMessage implements Message {

    private String topic;

    private byte[] body;

}
