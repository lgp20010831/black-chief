package com.black.core.sql.code.ill;

import com.black.core.sql.SQLSException;

public class StopSqlInvokeException extends SQLSException {


    public StopSqlInvokeException() {
    }

    public StopSqlInvokeException(String message) {
        super(message);
    }
}
