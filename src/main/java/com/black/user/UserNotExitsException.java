package com.black.user;

public class UserNotExitsException extends RuntimeException{

    public UserNotExitsException() {
    }

    public UserNotExitsException(String message) {
        super(message);
    }

    public UserNotExitsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotExitsException(Throwable cause) {
        super(cause);
    }
}
