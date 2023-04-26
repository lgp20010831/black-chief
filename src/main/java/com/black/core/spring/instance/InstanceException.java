package com.black.core.spring.instance;

public class InstanceException extends RuntimeException{


    public InstanceException(String message) {
        super(message);
    }

    public InstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
