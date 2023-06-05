package com.black.compile.sj;

/**
 * @author 李桂鹏
 * @create 2023-06-05 17:16
 */
@SuppressWarnings("all")
public class UnableCompileSjException extends Exception{

    public UnableCompileSjException(String message) {
        super(message);
    }

    public UnableCompileSjException(String message, Throwable cause) {
        super(message, cause);
    }
}
