package com.black.core.sql.xml;

import com.black.core.sql.SQLSException;

public class SqlXmlParseException extends SQLSException {

    public SqlXmlParseException() {
    }

    public SqlXmlParseException(String message) {
        super(message);
    }

    public SqlXmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlXmlParseException(Throwable cause) {
        super(cause);
    }
}
