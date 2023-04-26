package com.black.core.factory;

public class UnPowerFactoryException extends RuntimeException {

    public UnPowerFactoryException(String message) {
        super(message);
    }

    public UnPowerFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnPowerFactoryException(Throwable cause) {
        super(cause);
    }
}
