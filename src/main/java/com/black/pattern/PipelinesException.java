package com.black.pattern;

public class PipelinesException extends RuntimeException{


    public PipelinesException() {
    }

    public PipelinesException(String message) {
        super(message);
    }

    public PipelinesException(String message, Throwable cause) {
        super(message, cause);
    }

    public PipelinesException(Throwable cause) {
        super(cause);
    }
}
