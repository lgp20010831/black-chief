package com.black.core.io.socket;

public class SocketFactoryException extends RuntimeException{


    public SocketFactoryException() {
    }

    public SocketFactoryException(String message) {
        super(message);
    }

    public SocketFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketFactoryException(Throwable cause) {
        super(cause);
    }
}
