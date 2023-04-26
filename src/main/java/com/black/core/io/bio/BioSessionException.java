package com.black.core.io.bio;

public class BioSessionException extends RuntimeException{


    public BioSessionException() {
    }

    public BioSessionException(String message) {
        super(message);
    }

    public BioSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BioSessionException(Throwable cause) {
        super(cause);
    }
}
