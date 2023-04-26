package com.black.rpc;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.rpc.request.PrepareRequest;
import com.black.rpc.response.PrepareResponse;

import java.io.IOException;

public class RpcUtils {

    public static boolean isComplete(Prepare prepare) throws IOException {
        int sendSize = prepare.getBodySize();
        DataByteBufferArrayInputStream bodyInput = prepare.getBodyInput();
        return sendSize <= bodyInput.available();
    }

    public static PrepareRequest deserializePrepareRequest(DataByteBufferArrayInputStream in) throws IOException {
        PrepareRequest prepareRequest = new PrepareRequest();
        int type = in.readInt();
        prepareRequest.setType(type);
        String requestId = RpcFormat.readRequestId(in);
        prepareRequest.setRequestId(requestId);
        String requestMethod = RpcFormat.readRequestMethod(in);
        prepareRequest.setMethodName(requestMethod);
        prepareRequest.setBodySize(in.readInt());
        prepareRequest.setBodyInput(in);
        return prepareRequest;
    }

    public static PrepareResponse deserializePrepareResponse(DataByteBufferArrayInputStream in) throws IOException {
        PrepareResponse prepareResponse = new PrepareResponse();
        int type = in.readInt();
        prepareResponse.setType(type);
        String requestId = RpcFormat.readRequestId(in);
        prepareResponse.setRequestId(requestId);
        int state = in.readInt();
        prepareResponse.setState(state);
        prepareResponse.setBodySize(in.readInt());
        prepareResponse.setBodyInput(in);
        return prepareResponse;
    }
}
