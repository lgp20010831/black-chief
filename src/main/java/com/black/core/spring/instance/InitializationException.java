package com.black.core.spring.instance;

public class InitializationException extends InstanceException{
    public InitializationException(String message) {
        super(message);
    }


    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
