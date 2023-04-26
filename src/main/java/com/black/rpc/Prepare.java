package com.black.rpc;

import com.black.io.in.DataByteBufferArrayInputStream;

import java.io.IOException;

public interface Prepare {

    DataByteBufferArrayInputStream getBodyInput();

    int getBodySize();

    int bodyBufferSize() throws IOException;

    void appendBody(byte[] body) throws IOException;
}
