package com.black.nio.code;

public class AttysNioException extends RuntimeException{


    public AttysNioException() {
    }

    public AttysNioException(String message) {
        super(message);
    }

    public AttysNioException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttysNioException(Throwable cause) {
        super(cause);
    }
}
