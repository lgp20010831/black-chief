package com.black.rpc;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.ill.CreateRequestException;
import com.black.rpc.ill.NoMethodException;
import com.black.rpc.log.Log;
import com.black.rpc.request.Request;
import com.black.rpc.response.CommonResponse;
import com.black.rpc.response.Response;
import lombok.NonNull;

public class RpcThrowableHandler {


    public static Response resolveThrowable(@NonNull Throwable throwable, Request request, RpcConfiguration configuration){
        if (throwable instanceof CreateRequestException){
            return createNoCreateRequestResponse((CreateRequestException) throwable, configuration);
        }else if (throwable instanceof NoMethodException){
            return createNoMethodResponse((NoMethodException) throwable, request);
        }else {
            return createServerErrorResponse(request);
        }
    }

    private static Response createServerErrorResponse(Request request){
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setState(RpcState.ERROR);
        commonResponse.setRequestId(request.getRequestId());
        commonResponse.setRequestType(request.getRequestType());
        String errorMsg = "an irreparable exception occurred during the processing of business by the server";
        commonResponse.setBodySize(DataByteBufferArrayOutputStream.getUtfBytesLen(errorMsg));
        commonResponse.setBody(errorMsg);
        return commonResponse;
    }

    private static Response createNoMethodResponse(NoMethodException e, Request request){
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setState(RpcState.NO_METHOD);
        commonResponse.setRequestId(request.getRequestId());
        commonResponse.setRequestType(request.getRequestType());
        String errorMsg = e.getMessage();
        commonResponse.setBodySize(DataByteBufferArrayOutputStream.getUtfBytesLen(errorMsg));
        commonResponse.setBody(errorMsg);
        return commonResponse;
    }

    private static Response createNoCreateRequestResponse(CreateRequestException e, RpcConfiguration configuration){
        Log log = configuration.getLog();
        log.error("无效请求, 不会作出响应");
        return null;
    }

}
