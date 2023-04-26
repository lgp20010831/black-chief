package com.black.rpc.ill;

public class NoMethodException extends Exception{

    public NoMethodException() {
    }

    public NoMethodException(String message) {
        super(message);
    }

    public NoMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMethodException(Throwable cause) {
        super(cause);
    }
}
