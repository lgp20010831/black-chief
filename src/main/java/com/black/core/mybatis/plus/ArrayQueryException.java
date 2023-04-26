package com.black.core.mybatis.plus;

public class ArrayQueryException extends RuntimeException{


    public ArrayQueryException(String message) {
        super(message);
    }

    public ArrayQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArrayQueryException(Throwable cause) {
        super(cause);
    }
}
