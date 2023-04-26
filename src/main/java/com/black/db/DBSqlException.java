package com.black.db;

public class DBSqlException extends RuntimeException{


    public DBSqlException() {
    }

    public DBSqlException(String message) {
        super(message);
    }

    public DBSqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBSqlException(Throwable cause) {
        super(cause);
    }
}
