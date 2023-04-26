package com.black.core.aop.servlet.encryption;

public class CiphertextOperationException extends RuntimeException{


    public CiphertextOperationException() {
    }

    public CiphertextOperationException(String message) {
        super(message);
    }

    public CiphertextOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CiphertextOperationException(Throwable cause) {
        super(cause);
    }
}
