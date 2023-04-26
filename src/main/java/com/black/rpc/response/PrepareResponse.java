package com.black.rpc.response;

import com.black.rpc.RpcRequestType;
import com.black.rpc.RpcState;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Setter @Getter
public class PrepareResponse extends AbstractPrepare {

    private int type;

    private int state;

    private String requestId;

    public Response toResponse() throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setRequestType(RpcRequestType.typeOf(type));
        commonResponse.setRequestId(requestId);
        commonResponse.setState(RpcState.typeOf(state));
        commonResponse.setBodySize(bodySize);
        commonResponse.setBody(bodyInput.readUnrestrictedUtf());
        return commonResponse;
    }


}
