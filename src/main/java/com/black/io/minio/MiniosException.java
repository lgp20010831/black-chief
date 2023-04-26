package com.black.io.minio;

public class MiniosException extends RuntimeException{


    public MiniosException() {
    }

    public MiniosException(String message) {
        super(message);
    }

    public MiniosException(String message, Throwable cause) {
        super(message, cause);
    }

    public MiniosException(Throwable cause) {
        super(cause);
    }
}
