package com.black.core.sql;

//代替 SQLException
public class SQLSException extends RuntimeException {


    public SQLSException() {
    }

    public SQLSException(String message) {
        super(message);
    }

    public SQLSException(String message, Throwable cause) {
        super(message, cause);
    }

    public SQLSException(Throwable cause) {
        super(cause);
    }
}
