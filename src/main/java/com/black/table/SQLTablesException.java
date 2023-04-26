package com.black.table;

public class SQLTablesException extends RuntimeException {


    public SQLTablesException() {
    }

    public SQLTablesException(String message) {
        super(message);
    }

    public SQLTablesException(String message, Throwable cause) {
        super(message, cause);
    }

    public SQLTablesException(Throwable cause) {
        super(cause);
    }
}
