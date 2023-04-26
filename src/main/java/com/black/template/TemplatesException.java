package com.black.template;

public class TemplatesException extends RuntimeException{


    public TemplatesException() {
    }

    public TemplatesException(String message) {
        super(message);
    }

    public TemplatesException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplatesException(Throwable cause) {
        super(cause);
    }
}
