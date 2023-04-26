package com.black.throwable;

//剪切板异常, 在通过 JQBUtils 操作过程会被抛出, 通常封装了其他的异常,例如 io 异常
public class JQBException extends RuntimeException{


    public JQBException() {
    }

    public JQBException(String message) {
        super(message);
    }

    public JQBException(String message, Throwable cause) {
        super(message, cause);
    }

    public JQBException(Throwable cause) {
        super(cause);
    }
}
