package com.black.user;

public class UsersException extends RuntimeException{

    public UsersException() {
    }

    public UsersException(String message) {
        super(message);
    }

    public UsersException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsersException(Throwable cause) {
        super(cause);
    }
}
