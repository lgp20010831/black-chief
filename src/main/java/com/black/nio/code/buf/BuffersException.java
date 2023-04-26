package com.black.nio.code.buf;

public class BuffersException extends RuntimeException{


    public BuffersException() {
    }

    public BuffersException(String message) {
        super(message);
    }

    public BuffersException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuffersException(Throwable cause) {
        super(cause);
    }
}
