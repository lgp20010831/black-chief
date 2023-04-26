package com.black.rpc.demo;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.rpc.RpcUtils;
import com.black.rpc.request.PrepareRequest;
import com.black.rpc.request.Request;
import com.black.rpc.request.RequestBuilder;

import java.io.IOException;

public class IOTEST {


    public static void main(String[] args) throws IOException {
        Request request = RequestBuilder.fetch("userList", "supplier");
        DataByteBufferArrayInputStream in = request.getInputStream();
        PrepareRequest prepareRequest = RpcUtils.deserializePrepareRequest(in);
        System.out.println(prepareRequest.getBodyInput().readUTF());
        System.out.println(prepareRequest);
    }
}
