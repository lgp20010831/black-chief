package com.black.mq_v2;

public class MQTTException extends RuntimeException{

    public MQTTException() {
    }

    public MQTTException(String message) {
        super(message);
    }

    public MQTTException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQTTException(Throwable cause) {
        super(cause);
    }
}
