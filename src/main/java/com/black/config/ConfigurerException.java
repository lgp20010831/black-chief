package com.black.config;

public class ConfigurerException extends RuntimeException{

    public ConfigurerException() {
    }

    public ConfigurerException(String message) {
        super(message);
    }

    public ConfigurerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurerException(Throwable cause) {
        super(cause);
    }
}
