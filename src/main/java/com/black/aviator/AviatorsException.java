package com.black.aviator;

public class AviatorsException extends RuntimeException{


    public AviatorsException() {
    }

    public AviatorsException(String message) {
        super(message);
    }

    public AviatorsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AviatorsException(Throwable cause) {
        super(cause);
    }
}
