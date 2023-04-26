package com.black.rpc.inter;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.core.query.MethodWrapper;

import java.io.IOException;

public interface RpcMessageConvertHandler {


    Object convertRequest(DataByteBufferArrayInputStream inputStream, MethodWrapper mw) throws IOException;


    Object convertResponse(DataByteBufferArrayInputStream inputStream, MethodWrapper mw) throws IOException;


}
