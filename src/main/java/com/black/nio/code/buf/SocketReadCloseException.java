package com.black.nio.code.buf;

public class SocketReadCloseException extends Exception{


    public SocketReadCloseException() {
    }

    public SocketReadCloseException(String message) {
        super(message);
    }

    public SocketReadCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketReadCloseException(Throwable cause) {
        super(cause);
    }
}
