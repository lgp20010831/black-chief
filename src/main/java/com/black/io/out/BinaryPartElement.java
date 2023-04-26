package com.black.io.out;

import com.black.io.in.JHexByteArrayInputStream;

import java.io.IOException;
import java.io.Serializable;

public interface BinaryPartElement extends Serializable {

    String getName();

    JHexByteArrayInputStream getInputStream();

    int size() throws IOException;

    byte[] buf();
}
