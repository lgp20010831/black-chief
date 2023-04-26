package com.black.rpc.request;

import com.black.rpc.Rpc;

import java.io.IOException;

public interface Request extends Rpc {


    String getMethodName();

    byte[] toByteArray() throws IOException;


}
