package com.black.io.in;

import java.io.IOException;

public class ReadEndUnableParseException extends IOException {


    public ReadEndUnableParseException() {
    }

    public ReadEndUnableParseException(String message) {
        super(message);
    }

    public ReadEndUnableParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadEndUnableParseException(Throwable cause) {
        super(cause);
    }
}
