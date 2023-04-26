package com.black.rpc;

import com.black.io.in.DataByteBufferArrayInputStream;

import java.io.IOException;

public interface Rpc {

    RpcRequestType getRequestType();

    String getRequestId();

    int getBodySize();

    void addParam(String newBody);

    Object getParam();

    byte[] toByteArray() throws IOException;

    default DataByteBufferArrayInputStream getInputStream() throws IOException {
        return new DataByteBufferArrayInputStream(toByteArray());
    }

}
