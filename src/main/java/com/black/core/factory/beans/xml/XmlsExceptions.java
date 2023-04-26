package com.black.core.factory.beans.xml;

public class XmlsExceptions extends RuntimeException{


    public XmlsExceptions() {
    }

    public XmlsExceptions(String message) {
        super(message);
    }

    public XmlsExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlsExceptions(Throwable cause) {
        super(cause);
    }
}
