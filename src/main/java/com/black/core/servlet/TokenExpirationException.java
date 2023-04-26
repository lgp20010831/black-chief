package com.black.core.servlet;

public class TokenExpirationException extends Exception {

    public TokenExpirationException(String message) {
        super(message);
    }

    public TokenExpirationException() {
    }
}
