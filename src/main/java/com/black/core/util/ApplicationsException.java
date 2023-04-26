package com.black.core.util;

public class ApplicationsException extends RuntimeException{

    public ApplicationsException() {
    }

    public ApplicationsException(String message, Object... params) {
        super(TextUtils.parseContent(message, params));
    }

    public ApplicationsException(String message, Throwable cause, Object... params) {
        super(TextUtils.parseContent(message, params), cause);
    }

    public ApplicationsException(Throwable cause) {
        super(cause);
    }
}
