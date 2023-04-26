package com.black.throwable;

public class InterceptException extends Exception{

    public InterceptException() {
    }

    public InterceptException(String message) {
        super(message);
    }

    public InterceptException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterceptException(Throwable cause) {
        super(cause);
    }
}
