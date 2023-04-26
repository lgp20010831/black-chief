package com.black.rpc.ill;

public class CreateRequestException extends Exception{


    public CreateRequestException() {
    }

    public CreateRequestException(String message) {
        super(message);
    }

    public CreateRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateRequestException(Throwable cause) {
        super(cause);
    }
}
