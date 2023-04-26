package com.black.core.io.socket;

public class SocketsException extends RuntimeException{


    public SocketsException() {
    }

    public SocketsException(String message) {
        super(message);
    }

    public SocketsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketsException(Throwable cause) {
        super(cause);
    }
}
