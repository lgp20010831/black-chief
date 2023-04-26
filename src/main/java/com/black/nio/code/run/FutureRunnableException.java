package com.black.nio.code.run;

public class FutureRunnableException extends RuntimeException{


    public FutureRunnableException() {
    }

    public FutureRunnableException(String message) {
        super(message);
    }

    public FutureRunnableException(String message, Throwable cause) {
        super(message, cause);
    }

    public FutureRunnableException(Throwable cause) {
        super(cause);
    }
}
