package com.black.user;

public class CachesException extends RuntimeException{


    public CachesException() {
    }

    public CachesException(String message) {
        super(message);
    }

    public CachesException(String message, Throwable cause) {
        super(message, cause);
    }

    public CachesException(Throwable cause) {
        super(cause);
    }
}
