package com.black.socket.pool;

public class SocketPoolException extends RuntimeException {


    public SocketPoolException() {
    }

    public SocketPoolException(String message) {
        super(message);
    }

    public SocketPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketPoolException(Throwable cause) {
        super(cause);
    }
}
