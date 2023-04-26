package com.black.pool;

public class PoolStateException extends RuntimeException {


    public PoolStateException() {
    }

    public PoolStateException(String message) {
        super(message);
    }

    public PoolStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PoolStateException(Throwable cause) {
        super(cause);
    }
}
