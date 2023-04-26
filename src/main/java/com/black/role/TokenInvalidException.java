package com.black.role;

public class TokenInvalidException extends Exception{


    public TokenInvalidException() {
    }

    public TokenInvalidException(String message) {
        super(message);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenInvalidException(Throwable cause) {
        super(cause);
    }
}
