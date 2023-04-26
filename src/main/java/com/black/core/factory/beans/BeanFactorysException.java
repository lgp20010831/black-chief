package com.black.core.factory.beans;

public class BeanFactorysException extends RuntimeException{

    public BeanFactorysException() {
    }

    public BeanFactorysException(String message) {
        super(message);
    }

    public BeanFactorysException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanFactorysException(Throwable cause) {
        super(cause);
    }
}
