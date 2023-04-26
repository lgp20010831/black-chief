package com.black.core.aop.servlet.item;

public class ParsesException extends RuntimeException{


    public ParsesException() {
    }

    public ParsesException(String message) {
        super(message);
    }

    public ParsesException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsesException(Throwable cause) {
        super(cause);
    }
}
