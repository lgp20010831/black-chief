package com.black.callback.develop;

/**
 * @author 李桂鹏
 * @create 2023-05-17 16:02
 */
@SuppressWarnings("all")
public class DeveloperException extends RuntimeException{


    public DeveloperException() {
    }

    public DeveloperException(String message) {
        super(message);
    }

    public DeveloperException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeveloperException(Throwable cause) {
        super(cause);
    }
}
