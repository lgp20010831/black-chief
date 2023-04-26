package com.black.core.factory.beans.xml;

import java.io.InputStream;

public class XmlMessage {

    private String message;

    private InputStream stream;

    public XmlMessage(InputStream stream){
        this.stream = stream;
    }

    public XmlMessage(String message) {
        this.message = message;
    }

    public InputStream getStream() {
        return stream;
    }

    public String getMessage() {
        return message;
    }
}
