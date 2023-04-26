package com.black.rpc.response;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.RpcRequestType;
import com.black.rpc.RpcState;
import lombok.Setter;

import java.io.IOException;

@Setter
public class CommonResponse implements Response {
    private RpcState state;

    private RpcRequestType requestType;

    private String requestId;

    private Object body;

    private int bodySize;

    @Override
    public RpcState getState() {
        return state;
    }

    @Override
    public Object getParam() {
        return body;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        DataByteBufferArrayOutputStream outputStream = new DataByteBufferArrayOutputStream();
        outputStream.writeInt(requestType.getType());
        outputStream.writeUTF(requestId);
        int state = getState().getState();
        outputStream.writeInt(state);
        outputStream.writeInt(bodySize);
        String byteString = body == null ? "" : body.toString();
        outputStream.writeUnrestrictedUtf(byteString);
        return outputStream.toByteArray();
    }

    @Override
    public int getBodySize() {
        return bodySize;
    }

    @Override
    public void addParam(String newBody) {
        body += newBody;
    }

    @Override
    public RpcRequestType getRequestType() {
        return requestType;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }
}
