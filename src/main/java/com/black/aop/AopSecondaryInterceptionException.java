package com.black.aop;

/**
 * @author 李桂鹏
 * @create 2023-06-06 15:37
 */
@SuppressWarnings("all")
public class AopSecondaryInterceptionException extends RuntimeException{


    public AopSecondaryInterceptionException() {
    }

    public AopSecondaryInterceptionException(String message) {
        super(message);
    }

    public AopSecondaryInterceptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AopSecondaryInterceptionException(Throwable cause) {
        super(cause);
    }
}
