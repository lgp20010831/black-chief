package com.black.throwable;

public class BreakException extends RuntimeException{


    public BreakException() {
    }

    public BreakException(String message) {
        super(message);
    }

    public BreakException(String message, Throwable cause) {
        super(message, cause);
    }

    public BreakException(Throwable cause) {
        super(cause);
    }
}
