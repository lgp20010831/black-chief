package com.black.sql;

public class NativeQueryException extends RuntimeException {

    public NativeQueryException() {
    }

    public NativeQueryException(String message) {
        super(message);
    }

    public NativeQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NativeQueryException(Throwable cause) {
        super(cause);
    }
}
