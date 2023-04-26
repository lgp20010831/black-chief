package com.black.core.mybatis.plus;

public class ParseConfigurationException extends RuntimeException{


    public ParseConfigurationException() {
    }

    public ParseConfigurationException(String message) {
        super(message);
    }

    public ParseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseConfigurationException(Throwable cause) {
        super(cause);
    }
}
